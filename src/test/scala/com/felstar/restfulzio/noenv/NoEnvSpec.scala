package com.felstar.restfulzio.noenv

import zio.test._
import zio.http._
import zio.test.Assertion.equalTo

object NoEnvSpec extends ZIOSpecDefault {

  private val app = NoEnvApp()

  private def post(path: String, body: String) = app.runZIO(Request.post(path, Body.fromString(body)))

  def spec = suite("NoEnvSpec")(
    test("should be ok") {
      val req         = Request.get("/noenv")
      val expectedRes = app.runZIO(req).map(_.status)

      assertZIO(expectedRes)(equalTo(Status.Ok))
    },
    test("should say Hello from noenv") {
      val req  = Request.get("/noenv")

      // Using a for comprehension this time, and a smart assertion
      for {
        expectedBody <- app.runZIO(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "Hello from noenv")
    },
    test("should parse a simple post") {
      for {
        body1 <- post("/simplepost", """{"string": "hello"}""").flatMap(_.body.asString)
        body2 <- post("/simplepost2", """{"string": "hello"}""").flatMap(_.body.asString)
      } yield assertTrue(body1 == "post is SimplePost(hello)", body2 == "post is SimplePost(hello)")
    },
    test("should decode and encode json for a plain function") {
      for {
        dino  <- post("/simplepostcodec", """{"string": "Dino"}""").flatMap(_.body.asString)
        other <- post("/simplepostcodec", """{"string": "hello"}""").flatMap(_.body.asString)
        bad   <- post("/simplepostcodec", "not json")
      } yield assertTrue(dino == "\"hello Dino!!!!\"", other == "\"string is hello\"", bad.status == Status.BadRequest)
    }
  )
}
