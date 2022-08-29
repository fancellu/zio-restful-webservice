package com.felstar.restfulzio.noenv

import zio.test._
import zhttp.http._
import zio.test.Assertion.equalTo

object NoEnvSpec extends ZIOSpecDefault {

  val app = NoEnvApp()

  def spec = suite("NoEnvSpec")(
    test("should be ok") {
      val path        = !! / "noenv"
      val req         = Request(url = URL(path))
      val expectedRes = app(req).map(_.status)

      assertZIO(expectedRes)(equalTo(Status.Ok))
    },
    test("should say Hello from noenv") {
      val path = !! / "noenv"
      val req  = Request(url = URL(path))

      // Using a for comprehension this time, and a smart assertion
      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "Hello from noenv")
    }
  )
}
