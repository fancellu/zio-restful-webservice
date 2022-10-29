package com.felstar.restfulzio.counter

import zhttp.http._
import zio.{Ref, ZLayer}
import zio.test._

object CounterAppSpec extends ZIOSpecDefault {

  private val app = CounterApp()

  def spec = suite("CounterAppSpec")(
    test("up should work") {
      val path = !! / "up"
      val req = Request(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "101")
    },
    test("up plus down should work") {
      for {
        _ <- app(Request(url = URL(!! / "up"))).repeatN(2) // 3 ups in total
        _ <- app(Request(url = URL(!! / "down")))
        expectedBody <- app(Request(url = URL(!! / "get"))).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "102")
    },
    test("down should work") {
      val path = !! / "down"
      val req = Request(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "99")
    },
    test("get should work") {
      val path = !! / "get"
      val req = Request(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "100")
    },
    test("reset should work") {
      val path = !! / "reset"
      val req = Request(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody == "0")
    }

  ).provide(ZLayer.fromZIO(Ref.make(100)))
}
