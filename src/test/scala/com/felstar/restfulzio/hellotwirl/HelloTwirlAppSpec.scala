package com.felstar.restfulzio.hellotwirl

import zio.http._
import zio.ZLayer
import zio.test._

object HelloTwirlAppSpec extends ZIOSpecDefault {

  private val app = HelloTwirlApp()

  def spec = suite("HelloTwirlAppSpec")(
    test("should say run twirl template with default") {
      val path = !! / "hellotwirl"
      val req = Request.get(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody.contains("Just a twirl template with a passed parameter defaultstring"))
    },
    test("should say run twirl template with Dino") {
      val path = !! / "hellotwirl" / "Dino"
      val req = Request.get(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody.contains("Just a twirl template with a passed parameter Dino"))
    }
  )
}
