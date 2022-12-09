package com.felstar.restfulzio.helloworld

import zio.test._
import zio.http._
import zio.{Chunk, ZEnvironment, ZLayer}
import zio.test.Assertion.equalTo

object HelloWorldAppSpec extends ZIOSpecDefault {

  private val app = HelloWorldApp()

  def spec = suite("HelloWorldAppSpec")(
    test("should say Hello World!") {
      val path = !! / "myroot"
      val req = Request.get(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "Hello World!")
    },
    test("should say Hello Dino!") {
      val path = !! / "myroot" / "Dino"
      val req = Request.get(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "Hello Dino!")
    },
    test("should say Hello Dino and Milo!") {
      val path = !! / "myroot"
      val queryParams=QueryParams("name"->"Dino", "name" -> "Milo")
      val req = Request.get(url = URL(path, queryParams=queryParams))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "Hello Dino and Milo!")
    }

  ).provide(ZLayer.succeedEnvironment(ZEnvironment(
    Map(
      "helloworld" -> "myroot",
      "ignore" -> "ignored"
    )
  )))
}
