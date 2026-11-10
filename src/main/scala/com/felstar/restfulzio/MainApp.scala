package com.felstar.restfulzio

import com.felstar.restfulzio.actors.ActorsApp
import com.felstar.restfulzio.auth.CookieAuthApp
import com.felstar.restfulzio.client.ClientApp
import com.felstar.restfulzio.counter.CounterApp
import com.felstar.restfulzio.delay.DelayApp
import com.felstar.restfulzio.download.DownloadApp
import com.felstar.restfulzio.form.FormApp
import com.felstar.restfulzio.helloworld.HelloWorldApp
import com.felstar.restfulzio.noenv.NoEnvApp
import com.felstar.restfulzio.staticserver.StaticApp
import com.felstar.restfulzio.hellotwirl.HelloTwirlApp
import com.felstar.restfulzio.gemini.{GeminiImageApp, GeminiTextApp}
import com.felstar.restfulzio.openai.OpenAIModerationApp
import com.felstar.restfulzio.stream.StreamApp
import com.felstar.restfulzio.videos.InmemoryVideoRepo
import com.felstar.restfulzio.videos.VideoApp
import zio._
import zio.cache.{Cache, Lookup}
import zio.http._
import zio.logging.{ConsoleLoggerConfig, LogFormat, consoleLogger}
import zio.spark.parameter.localAllNodes
import zio.spark.sql.SparkSession

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MainApp extends ZIOAppDefault {

  // Catches failures and defects, logs them, and returns a plain 500, rather than a rendered stack trace
  def handleErrors[R](routes: Routes[R, Throwable]): Routes[R, Nothing] =
    routes.handleErrorCauseZIO { cause =>
      ZIO.logError(cause.squash.toString).as(Response.internalServerError)
    }

  override val bootstrap =
    Runtime.removeDefaultLoggers >>> consoleLogger(ConsoleLoggerConfig.default.copy(format = LogFormat.colored))

  // Alters the incoming request, before the routes get to see it
  val requestMiddleWare: Middleware[Any] =
    HandlerAspect.updateRequest(_.addHeader("Seen", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())))

  val userCache: URIO[Client, Cache[Int, Throwable, Response]] = Cache.make(
    capacity = 100,
    timeToLive = 15.seconds,
    lookup = Lookup(ClientApp.getUser)
  )

  val enableSpark=false

  lazy val sparkSession: ZLayer[Any, Throwable, SparkSession] =SparkSession.builder.master(localAllNodes).appName("app").asLayer

  // For `HelloWorldApp`
  val helloWorldConfig = ZLayer.succeedEnvironment(ZEnvironment(
    Map(
      "helloworld" -> "hello",
      "ignore" -> "ignored"
    )
  ))

  override val run = for {
    _ <- ZIO.logInfo("Starting up")
    args <- getArgs // to get command line params
    _ <- ZIO.logInfo(args.toString)
    helloWorldRoutes <- HelloWorldApp().provide(helloWorldConfig)
    routes = handleErrors(NoEnvApp() @@ requestMiddleWare ++ helloWorldRoutes ++ DownloadApp() ++
      CounterApp() ++ VideoApp() ++ ActorsApp() ++ HelloTwirlApp() ++ // (if (enableSpark) SparkApp() else Routes.empty)  ++
      DelayApp() ++ StreamApp() ++ ClientApp() ++ GeminiTextApp() ++ GeminiImageApp() ++ OpenAIModerationApp()
      ++ CookieAuthApp() ++ FormApp() ++ StaticApp())
    server1 = Server.serve(routes)
      .provide(
        Server.default,
        Client.default,
        // For `CounterApp`
        ZLayer.fromZIO(Ref.make(0)),
        // To use the H2 DB layer, provide the `PersistentVideoRepo.layer` layer instead
        InmemoryVideoRepo.layer,
        // PersistentVideoRepo.layer
        ZLayer.fromZIO(userCache),
        // For `ActorsApp`
        ActorsApp.layer,
//        if (enableSpark) sparkSession else ZLayer.die(new Throwable("bang"))
      )
    // A second, independent server, on another port
    server2 = Server.serve(
      Routes(
        Method.GET / "hello" -> Handler.text("world"),
        Method.GET / "world" -> Handler.text("hello")
      )
    ).provide(Server.defaultWithPort(8082))
    // If stdin is closed (docker, service, non interactive shell) we just keep running
    waitForEnter = Console.readLine("Press enter to stop the server\n")
      .catchAll(_ => ZIO.logInfo("No stdin, running until killed") *> ZIO.never) *>
      Console.printLine("Interrupting server")
    // Whichever finishes first wins, so a server that fails to start (e.g. port in use) fails the app
    servers = (server1 zipPar server2).unit.tapError(e => ZIO.logError(s"Server failed: $e"))
    _ <- servers.raceFirst(waitForEnter)
  } yield ()
}
