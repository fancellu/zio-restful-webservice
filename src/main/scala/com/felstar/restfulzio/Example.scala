package com.felstar.restfulzio

import zio._
import zio.http._
import zio.http.model._

object Example extends ZIOAppDefault {

  val logger: ZLogger[String, Unit] =
    ZLogger.default.map(_.toUpperCase).map(println)

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> Runtime.addLogger(logger)

  val app: HttpApp[Any, Nothing] =
    Http.collectZIO[Request] { case Method.GET -> !! / "test" =>
      ZIO.log("test").as(Response.text("Hello World!"))
    }

  val run =
    Server.serve(app).provide(Server.default)
}