package com.felstar.restfulzio.helloworld

import zio.test._
import zio.http._
import zio.{Chunk, Scope, ZEnvironment, ZLayer}

object HelloWorldAppSpec extends ZIOSpecDefault {

  private def bodyOf(path: String, queryParams: (String, String)*) = for {
    app  <- HelloWorldApp()
    url = URL(Path.decode(path), queryParams = QueryParams(queryParams.groupMap(_._1)(_._2).map { case (k, v) => k -> Chunk.fromIterable(v) }))
    body <- app.runZIO(Request.get(url)).flatMap(_.body.asString)
  } yield body

  def spec = suite("HelloWorldAppSpec")(
    test("should say Hello World!") {
      for {
        expectedBody <- bodyOf("/myroot")
      } yield assertTrue(expectedBody == "Hello World!")
    },
    test("should say Hello Dino!") {
      for {
        expectedBody <- bodyOf("/myroot/Dino")
      } yield assertTrue(expectedBody == "Hello Dino!")
    },
    test("should say Hello Dino and Milo!") {
      for {
        expectedBody <- bodyOf("/myroot", "name" -> "Dino", "name" -> "Milo")
      } yield assertTrue(expectedBody == "Hello Dino and Milo!")
    },
    test("should say Hello World! when there are query params, but no name") {
      for {
        expectedBody <- bodyOf("/myroot", "foo" -> "bar")
      } yield assertTrue(expectedBody == "Hello World!")
    },
    test("should not answer on any other root") {
      for {
        app      <- HelloWorldApp()
        response <- app.runZIO(Request.get("/hello"))
      } yield assertTrue(response.status == Status.NotFound)
    }

  ).provide(ZLayer.succeedEnvironment(ZEnvironment(
    Map(
      "helloworld" -> "myroot",
      "ignore" -> "ignored"
    )
  )), Scope.default)
}
