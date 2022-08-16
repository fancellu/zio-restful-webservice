package com.felstar.restfulzio.delay

import zhttp.http._
import zio.Console._
import zio.{Random, ZIO, durationInt}


/**
 * An http app that:
 *   - Accepts a `Request` and returns a `Response`
 *   - Does not fail
 *   - Does not use Env
 *   Delays N seconds, useful when testing client code for timeout handling
 */
object DelayApp {
  def apply(): Http[Any, Nothing, Request, Response] = {

    def delay(n: Int=3): ZIO[Any, Nothing, Response] = {
      for {
        _ <- printLine(s"starting delay, waiting $n").orDie
        _ <- ZIO.sleep(n.second) // semantic blocking, but doesn't block any threads
        _ <- printLine("delay ended").orDie
        randomInt <- Random.nextIntBetween(1,10000)
      } yield Response.text(s"Hello from delay, slept for $n seconds, random int: $randomInt")
    }

    Http.collectZIO[Request] {
      // GET /delay/n
      case Method.GET -> !! / "delay" / int(n) =>
        delay(n)
      // GET /delay
      case Method.GET -> !! / "delay" =>
        delay()
    }
  }
}
