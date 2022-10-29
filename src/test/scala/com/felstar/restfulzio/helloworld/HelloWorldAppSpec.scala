package com.felstar.restfulzio.helloworld

import zio.test._
import zhttp.http._
import zio.ZLayer
import zio.test.Assertion.equalTo

object HelloWorldAppSpec extends ZIOSpecDefault {

  private val app = HelloWorldApp()

  def spec = suite("HelloWorldAppSpec")(
    test("should say Hello World!") {
      val path = !! / "myroot"
      val req = Request(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "Hello World!")
    },
    test("should say Hello Dino!") {
      val path = !! / "myroot" / "Dino"
      val req = Request(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "Hello Dino!")
    },
    test("should say Hello Dino and Milo!") {
      val path = !! / "myroot"
      val req = Request(url = URL(path, queryParams = Map("name"->List("Dino","Milo"))))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "Hello Dino and Milo!")
    }

  ).provide(ZLayer.succeed("myroot"))
}
