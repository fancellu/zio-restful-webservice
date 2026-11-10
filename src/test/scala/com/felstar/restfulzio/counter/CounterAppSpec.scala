package com.felstar.restfulzio.counter

import zio.http._
import zio.{Ref, Scope, ZLayer}
import zio.test._

object CounterAppSpec extends ZIOSpecDefault {

  private val app = CounterApp()

  private def bodyOf(url: String) = app.runZIO(Request.get(url)).flatMap(_.body.asString)

  def spec = suite("CounterAppSpec")(
    test("up should work") {
      for {
        expectedBody <- bodyOf("/up")
      } yield assertTrue(expectedBody == "101")
    },
    test("up/ should work") {
      for {
        expectedBody <- bodyOf("/up/")
      } yield assertTrue(expectedBody == "101")
    },
    test("up plus down should work") {
      for {
        _ <- bodyOf("/up").repeatN(2) // 3 ups in total
        _ <- bodyOf("/down")
        expectedBody <- bodyOf("/get")
      } yield assertTrue(expectedBody == "102")
    },
    test("down should work") {
      for {
        expectedBody <- bodyOf("/down")
      } yield assertTrue(expectedBody == "99")
    },
    test("get should work") {
      for {
        expectedBody <- bodyOf("/get")
      } yield assertTrue(expectedBody == "100")
    },
    test("reset should work") {
      for {
        expectedBody <- bodyOf("/reset")
      } yield assertTrue(expectedBody == "0")
    }

  ).provide(ZLayer.fromZIO(Ref.make(100)), Scope.default)
}
