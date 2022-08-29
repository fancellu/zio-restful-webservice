package com.felstar.restfulzio.counter

import com.felstar.restfulzio.MainApp
import zhttp.http._
import zio.{Ref, ZIO}

/** An http app that:
  *   - Accepts `Request` and returns a `Response`
  *   - Does not fail
  *   - Requires the `Ref[Int]` as the environment
  */
object CounterApp {

  def apply(): Http[Ref[Int], Nothing, Request, Response] =
    Http.fromZIO(ZIO.service[Ref[Int]]).flatMap { ref =>
      Http.collectZIO[Request] {
        case Method.GET -> !! / "up" =>
          for {
            i <- ref.updateAndGet(_ + 1)
            _ <- ZIO.logInfo(s"Incremented to $i")
          } yield Response.text(i.toString)
        case Method.GET -> !! / "down" =>
          ref
            .updateAndGet(_ - 1)
            .tap { i => ZIO.logInfo(s"Decremented to $i") }
            .map(_.toString)
            .map(Response.text)
        case Method.GET -> !! / "get" =>
          ref.get.map(_.toString).map(Response.text)
        case Method.GET -> !! / "reset" =>
          ref
            .updateAndGet(_ => 0)
            .tap { i => ZIO.logInfo(s"Reset to $i") }
            .map(_.toString)
            .map(Response.text)
      }
    }
}
