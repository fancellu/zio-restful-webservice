package com.felstar.restfulzio.delay

import zio.http._
import zio.Console.printLine
import zio.{Schedule, durationInt}
import zio.test._

object DelayAppSpec extends ZIOSpecDefault {

  // sandbox turns failures into 500 responses, as Routes can only be run once their errors are handled
  private val app = DelayApp().sandbox

  def spec = suite("DelayAppSpec")(
    test("should say Hello from delay, slept for 3 seconds") {
      val req = Request.get("/delay")

      for {
        expectedBodyFiber <- app.runZIO(req).flatMap(_.body.asString).fork
        _ <- TestClock.adjust(10.seconds)
        expectedBody <- expectedBodyFiber.join
      } yield assertTrue(expectedBody.contains("Hello from delay, slept for 3 seconds"))
    },
    test("should say Hello from delay, slept for 2 seconds") {
      val req = Request.get("/delay/2")

      for {
        expectedBodyFiber <- app.runZIO(req).flatMap(_.body.asString).fork
        _ <- TestClock.adjust(10.seconds)
        expectedBody <- expectedBodyFiber.join
      } yield assertTrue(expectedBody.contains("Hello from delay, slept for 2 seconds"))
    },
    test("should fail") {
      val req = Request.get("/bang")

      for {
        responseFiber <- app.runZIO(req).fork
        _ <- TestClock.adjust(10.seconds)
        response <- responseFiber.join
      } yield assertTrue(response.status == Status.InternalServerError)
    },
    test("should not fail, due to TestAspect.retry") {
      val req = Request.get("/bangrandomly")

      for {
        responseFiber <- app.runZIO(req).fork
        _ <- TestClock.adjust(10.seconds)
        response <- responseFiber.join
      } yield assertTrue(response.status == Status.Ok)
      // A retry replays the test with the same TestRandom values, so we need the live Random to ever get a different outcome.
      // The retries are capped, so that the test can never spin forever
    } @@ TestAspect.withLiveRandom @@ TestAspect.retry(
      (Schedule.spaced(10.millis) && Schedule.recurs(200)).tapOutput { case (count, _) => printLine(s"retried $count").orDie }
    )
  )
}
