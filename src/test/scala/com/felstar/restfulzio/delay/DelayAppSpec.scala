package com.felstar.restfulzio.delay

import zhttp.http._
import zio.Cause.Fail
import zio.Console.printLine
import zio.{Random, Schedule, ZLayer, durationInt}
import zio.test._

object DelayAppSpec extends ZIOSpecDefault {

  val app = DelayApp()

  def spec = suite("DelayAppSpec")(
    test("should say Hello from delay, slept for 3 seconds") {
      val path = !! / "delay"
      val req = Request(url = URL(path))

      for {
        expectedBodyFiber <- app(req).flatMap(_.bodyAsString).fork
        _ <- TestClock.adjust(10.seconds)
        expectedBody <- expectedBodyFiber.join
      } yield assertTrue(expectedBody.contains("Hello from delay, slept for 3 seconds"))
    },
    test("should say Hello from delay, slept for 2 seconds") {
      val path = !! / "delay" / "2"
      val req = Request(url = URL(path))

      for {
        expectedBodyFiber <- app(req).flatMap(_.bodyAsString).fork
        _ <- TestClock.adjust(10.seconds)
        expectedBody <- expectedBodyFiber.join
      } yield assertTrue(expectedBody.contains("Hello from delay, slept for 2 seconds"))
    },
    test("should fail") {
      val path = !! / "bang"
      val req = Request(url = URL(path))

      for {
        expectedBodyFiber <- app(req).flatMap(_.bodyAsString).fork
        _ <- TestClock.adjust(10.seconds)
        exitValue <- expectedBodyFiber.await
      } yield assertTrue(exitValue.isFailure)
    },
    test("should not fail, due to TestAspect.retry") {
      val path = !! / "bangrandomly"
      val req = Request(url = URL(path))

      for {
        expectedBodyFiber <- app(req).flatMap(_.bodyAsString).fork
        _ <- TestClock.adjust(10.seconds)
        exitValue <- expectedBodyFiber.await
      } yield assertTrue(exitValue.isSuccess)
    } @@ TestAspect.retry(Schedule.spaced(10.millis))
  )
}
