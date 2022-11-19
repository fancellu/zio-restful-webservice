package com.felstar.restfulzio.delay

import zio.Console._
import zio.{IO, Random, Task, UIO, ZIO, durationInt}
import zio.http._
import zio.http.model.Method


/**
 * An http app that:
 *   - Accepts a `Request` and returns a `Response`
 *   - Does not fail
 *   - Does not use Env
 *   Delays N seconds, useful when testing client code for timeout handling
 */
object DelayApp {
  def apply(): Http[Any, Throwable, Request, Response] = {

    def delay(n: Int=3): UIO[Response] = {
      for {
        _ <- printLine(s"starting delay, waiting $n").orDie
        _ <- ZIO.sleep(n.second) // semantic blocking, but doesn't block any threads
        _ <- printLine("delay ended").orDie
        randomInt <- Random.nextIntBetween(1,10000)
      } yield Response.text(s"Hello from delay, slept for $n seconds, random int: $randomInt")
    }

    // fails 9 out of 10 times, failure maps to 500 error
    def bangRandomly() : Task[Response] = {
      for {
        float <- Random.nextFloat
        _ <- if (float>0.1) printLineError(s"went bang $float") *> ZIO.fail(new Exception("bangRandomly")) else printLine(s"OK $float").unit
      } yield Response.text(s"OK")
    }

    Http.collectZIO[Request] {
      // GET /delay/n
      case Method.GET -> !! / "delay" / int(n) =>
        delay(n)
      // GET /delay
      case Method.GET -> !! / "delay" =>
        delay()
      case Method.GET -> !! / "bang" =>
        delay() *> ZIO.fail(new Exception("bang!!!"))
      case Method.GET -> !! / "bangrandomly" =>
        bangRandomly()
    }
  }
}
