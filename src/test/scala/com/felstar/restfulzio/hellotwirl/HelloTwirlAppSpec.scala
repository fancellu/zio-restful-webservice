package com.felstar.restfulzio.hellotwirl

import zio.http._
import zio.test._

object HelloTwirlAppSpec extends ZIOSpecDefault {

  private val app = HelloTwirlApp()

  def spec = suite("HelloTwirlAppSpec")(
    test("should say run twirl template with default") {
      val req = Request.get("/hellotwirl")

      for {
        expectedBody <- app.runZIO(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody.contains("Just a twirl template with a passed parameter defaultstring"))
    },
    test("should say run twirl template with Dino") {
      val req = Request.get("/hellotwirl/Dino")

      for {
        expectedBody <- app.runZIO(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody.contains("Just a twirl template with a passed parameter Dino"))
    }
  )
}
