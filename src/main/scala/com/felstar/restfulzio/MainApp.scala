package com.felstar.restfulzio

import com.felstar.restfulzio.actors.ActorsApp
import com.felstar.restfulzio.auth.CookieAuthApp
import com.felstar.restfulzio.client.ClientApp
import com.felstar.restfulzio.counter.CounterApp
import com.felstar.restfulzio.delay.DelayApp
import com.felstar.restfulzio.download.DownloadApp
import com.felstar.restfulzio.helloworld.HelloWorldApp
import com.felstar.restfulzio.noenv.NoEnvApp
import com.felstar.restfulzio.staticserver.StaticApp
import com.felstar.restfulzio.hellotwirl.HelloTwirlApp
import com.felstar.restfulzio.openai.OpenAICompletionApp
import com.felstar.restfulzio.spark.SparkApp
import com.felstar.restfulzio.stream.StreamApp
import com.felstar.restfulzio.videos.{InmemoryVideoRepo, PersistentVideoRepo, VideoApp}
import zio.http._
import zio._
import zio.cache.{Cache, Lookup}
import zio.http.{Http, Middleware, Request, Response, Server}
import zio.http.middleware.HttpMiddleware
import zio.http.model.Status
import zio.logging.{LogFormat, console}
import zio.spark.parameter.localAllNodes
import zio.spark.sql.SparkSession

import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MainApp extends ZIOAppDefault {

  // catches errors and stops the default render of stack trace

  val errorMiddleware = new HttpMiddleware[Any, Throwable] {

    override def apply[R1 <: Any, E1 >: Throwable](http: Http[R1, E1, Request, Response])(implicit trace: Trace): Http[R1, E1, Request, Response] =
      http.catchAll { ex =>
          val zio: ZIO[Any, IOException, Response] = for {
            _ <- ZIO.logError(ex.toString)
          } yield Response.status(Status.InternalServerError)
          Http.responseZIO(zio)
        }
  }

  val middlewares = errorMiddleware // ++ Middleware.dropTrailingSlash

  val logger =
    Runtime.removeDefaultLoggers >>> console(LogFormat.colored)

  val requestMiddleWare=Middleware.identity[Request, Response].contramap[Request](_.addHeader("Seen", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())))

  val userCache: URIO[Client, Cache[Int, Throwable, Response]] = Cache.make(
    capacity = 100,
    timeToLive = 15.seconds,
    lookup = Lookup(ClientApp.getUser)
  )

  val enableSpark=true

  lazy val sparkSession: ZLayer[Any, Throwable, SparkSession] =SparkSession.builder.master(localAllNodes).appName("app").asLayer

  override val run = for {
    _ <- ZIO.logInfo("Starting up").provide(logger)
    args <- getArgs // to get command line params
    _ <- ZIO.logInfo(args.toString).provide(logger)
    serverFibre <- Server.serve((NoEnvApp()  @@ requestMiddleWare ++ HelloWorldApp() ++ DownloadApp() ++
      CounterApp() ++ VideoApp() ++ ActorsApp() ++ HelloTwirlApp() ++ (if (enableSpark) SparkApp() else Http.empty)  ++
      DelayApp() ++ StreamApp() ++ ClientApp() ++ OpenAICompletionApp() ++ CookieAuthApp() ++ StaticApp()) @@ middlewares)
      .provide(
        Server.default,
        Scope.default,
        Client.default,
        // For `CounterApp`
        ZLayer.fromZIO(Ref.make(0)),
        // For `HelloWorldApp`
        ZLayer.succeedEnvironment(ZEnvironment(
          Map(
            "helloworld" -> "hello",
            "ignore" -> "ignored"
          )
        )),
        // To use the H2 DB layer, provide the `PersistentVideoRepo.layer` layer instead
        InmemoryVideoRepo.layer,
        // PersistentVideoRepo.layer
        logger,
        ZLayer.fromZIO(userCache),
        if (enableSpark) sparkSession else ZLayer.die(new Throwable("bang"))
      )
      .fork
    _ <- Console.readLine("Press enter to stop the server\n")
    _ <- Console.printLine("Interrupting server")
    _ <- serverFibre.interrupt
  } yield ()
}
