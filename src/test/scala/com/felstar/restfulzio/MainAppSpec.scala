package com.felstar.restfulzio

import zio.ZIO
import zio.http._
import zio.test._

object MainAppSpec extends ZIOSpecDefault {

  private val app = MainApp.handleErrors(
    Routes(
      Method.GET / "fail" -> handler(ZIO.fail(new Exception("typed failure")): ZIO[Any, Throwable, Response]),
      Method.GET / "die" -> handler(ZIO.dieMessage("a defect"): ZIO[Any, Throwable, Response])
    )
  )

  def spec = suite("MainAppSpec")(
    test("a failure becomes a plain 500") {
      for {
        response <- app.runZIO(Request.get("/fail"))
        body     <- response.body.asString
      } yield assertTrue(response.status == Status.InternalServerError, !body.contains("typed failure"))
    },
    test("a defect becomes a plain 500 too") {
      for {
        response <- app.runZIO(Request.get("/die"))
        body     <- response.body.asString
      } yield assertTrue(response.status == Status.InternalServerError, !body.contains("defect"))
    }
  )
}
