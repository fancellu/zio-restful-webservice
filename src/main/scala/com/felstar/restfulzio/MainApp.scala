package com.felstar.restfulzio

import com.felstar.restfulzio.actors.ActorsApp
import com.felstar.restfulzio.client.ClientApp
import com.felstar.restfulzio.client.ClientApp.getUser
import com.felstar.restfulzio.counter.CounterApp
import com.felstar.restfulzio.delay.DelayApp
import com.felstar.restfulzio.download.DownloadApp
import com.felstar.restfulzio.hellotwirl.HelloTwirlApp
import com.felstar.restfulzio.helloworld.HelloWorldApp
import com.felstar.restfulzio.noenv.NoEnvApp
import com.felstar.restfulzio.staticserver.StaticApp
import com.felstar.restfulzio.actors.ActorsApp
import com.felstar.restfulzio.stream.StreamApp
import com.felstar.restfulzio.videos.{InmemoryVideoRepo, PersistentVideoRepo, VideoApp}
import zhttp.http.{Http, HttpApp, Middleware, Request, Response, Status}
import zhttp.http.middleware.HttpMiddleware
import zhttp.service.{ChannelFactory, EventLoopGroup, Server}
import zio._
import zio.cache.{Cache, Lookup}
import zio.logging.{LogFormat, console}

import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MainApp extends ZIOAppDefault {

  // catches errors and stops the default render of stack trace

  val errorMiddleware = new HttpMiddleware[Any, Throwable] {
    override def apply[R1 <: Any, E1 >: Throwable](
        http: HttpApp[R1, E1]
    ) =
      http
        .catchAll { ex =>
          val zio: ZIO[Any, IOException, Response] = for {
            _ <- ZIO.logError(ex.toString)
          } yield Response.status(Status.InternalServerError)
          Http.responseZIO(zio)
        }
  }

  val middlewares = errorMiddleware // ++ Middleware.dropTrailingSlash

  private val logger =
    Runtime.removeDefaultLoggers >>> console(LogFormat.colored)

  val requestMiddleWare=Middleware.identity[Request, Response].contramap[Request](_.addHeader("Seen", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())))

  val serverSetup = Server.port(8080) ++ Server.app((NoEnvApp()  ++ HelloWorldApp() ++ DownloadApp() ++
    CounterApp() ++ VideoApp() ++ HelloTwirlApp() ++ ActorsApp() ++
    DelayApp() ++ StreamApp() ++ ClientApp() ++ StaticApp()) @@ middlewares)

  val userCache: URIO[EventLoopGroup with ChannelFactory, Cache[Int, Throwable, Response]] = Cache.make(
    capacity = 100,
    timeToLive = 15.seconds,
    lookup = Lookup(ClientApp.getUser)
  )

  def run = for {
    _ <- ZIO.logInfo("Starting up").provide(logger)
    zioAppArgs <- ZIO.service[ZIOAppArgs] // to get command line params
    _ <- ZIO.logInfo(zioAppArgs.toString).provide(logger)
    serverFibre <- serverSetup.startDefault
      .provide(
        // For `CounterApp`
        ZLayer.fromZIO(Ref.make(0)),
        // For `HelloWorldApp`
        ZLayer.succeed("hello"),
        // To use the H2 DB layer, provide the `PersistentVideoRepo.layer` layer instead
        InmemoryVideoRepo.layer,
        // PersistentVideoRepo.layer
        // for Client
        ChannelFactory.auto,
        EventLoopGroup.auto(),
        logger,
        ZLayer.fromZIO(userCache)
      )
      .fork
    _ <- Console.readLine("Press enter to stop the server\n")
    _ <- Console.printLine("Interrupting server")
    _ <- serverFibre.interrupt
  } yield ()
}
