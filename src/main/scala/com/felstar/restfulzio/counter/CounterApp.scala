package com.felstar.restfulzio.counter

import zio._
import zio.http._

/** Routes that:
  *   - Accept `Request` and return a `Response`
  *   - Do not fail
  *   - Require the `Ref[Int]` as the environment
  */
object CounterApp {

  private def withCounter(f: Ref[Int] => UIO[Int]): URIO[Ref[Int], Response] =
    ZIO.serviceWithZIO[Ref[Int]](f).map(i => Response.text(i.toString))

  def apply(): Routes[Ref[Int], Nothing] = Routes(
    Method.GET / "up" -> handler {
      withCounter(_.updateAndGet(_ + 1).tap(i => ZIO.logInfo(s"Incremented to $i")))
    },
    Method.GET / "down" -> handler {
      withCounter(_.updateAndGet(_ - 1).tap(i => ZIO.logInfo(s"Decremented to $i")))
    },
    Method.GET / "get" -> handler {
      withCounter(_.get)
    },
    Method.GET / "reset" -> handler {
      withCounter(_.updateAndGet(_ => 0).tap(i => ZIO.logInfo(s"Reset to $i")))
    }
  )
}
