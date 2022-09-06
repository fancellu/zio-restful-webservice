package com.felstar.restfulzio

import com.felstar.restfulzio.client.ClientApp
import com.felstar.restfulzio.counter.CounterApp
import com.felstar.restfulzio.delay.DelayApp
import com.felstar.restfulzio.download.DownloadApp
import com.felstar.restfulzio.hellotwirl.HelloTwirlApp
import com.felstar.restfulzio.helloworld.HelloWorldApp
import com.felstar.restfulzio.noenv.NoEnvApp
import com.felstar.restfulzio.staticserver.example.StaticApp
import com.felstar.restfulzio.stream.StreamApp
import com.felstar.restfulzio.videos.{InmemoryVideoRepo, PersistentVideoRepo, VideoApp}
import zhttp.http.{Http, HttpApp, Middleware, Response, Status}
import zhttp.http.middleware.HttpMiddleware
import zhttp.service.{ChannelFactory, EventLoopGroup, Server}
import zio.logging.LogFormat._
import zio._
import zio.logging.{LogColor, LogFormat, console}

import java.io.IOException

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

  val myLogFormat =  label("xx", timestamp.fixed(32)).color(LogColor.BLUE) // |-|
//    label("level", level).highlight |-|
//    label("message", quoted(line)).highlight


  private val logger =
    Runtime.removeDefaultLoggers >>> console(LogFormat.colored)

  def run = {
    ZIO.logInfo("Starting up").provide(logger) *>
    Server
      .start(
        port = 8080,
        http =
          ((NoEnvApp() ++ HelloWorldApp() ++ DownloadApp() ++ CounterApp() ++ VideoApp() ++ HelloTwirlApp() ++
            DelayApp() ++ StreamApp() ++ ClientApp() ++ StaticApp()) @@ middlewares)
      )
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
        logger
      )
      .exitCode
  }
}
