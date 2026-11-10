package com.felstar.restfulzio.delay

import zio.Console._
import zio.{Random, Task, UIO, ZIO, durationInt}
import zio.http._


/**
 * Routes that:
 *   - Accept a `Request` and return a `Response`
 *   - May fail, /bang always does, /bangrandomly mostly does
 *   - Do not use Env
 *   Delays N seconds, useful when testing client code for timeout handling
 */
object DelayApp {
  def apply(): Routes[Any, Throwable] = {

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

    def bang(): Task[Response] = delay() *> ZIO.fail(new Exception("bang!!!"))

    Routes(
      // GET /delay/n
      Method.GET / "delay" / int("n") -> handler { (n: Int, _: Request) =>
        delay(n)
      },
      // GET /delay
      Method.GET / "delay" -> handler(delay()),
      Method.GET / "bang" -> handler(bang()),
      Method.GET / "bangrandomly" -> handler(bangRandomly())
    )
  }
}
