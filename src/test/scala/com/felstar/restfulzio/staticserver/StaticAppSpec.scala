package com.felstar.restfulzio.staticserver

import zio.http._
import zio.test._

object StaticAppSpec extends ZIOSpecDefault {

  private val app = StaticApp()

  def spec = suite("StaticAppSpec")(
    test("should serve a file from the static dir") {
      val req = Request.get(url = URL(!! / "test.txt"))

      for {
        body <- app(req).flatMap(_.body.asString)
      } yield assertTrue(body.contains("Hello from static dir file test.txt"))
    },
    test("should list a sub directory") {
      val req = Request.get(url = URL(!! / "subdir"))

      for {
        body <- app(req).flatMap(_.body.asString)
      } yield assertTrue(body.contains("test3.txt"))
    },
    test("should not serve files outside the static dir") {
      val req = Request.get(url = URL(!! / ".." / "build.sbt"))

      for {
        exit <- app(req).exit
      } yield assertTrue(exit.isFailure)
    },
    test("should not list directories outside the static dir") {
      val req = Request.get(url = URL(!! / ".." / ".."))

      for {
        exit <- app(req).exit
      } yield assertTrue(exit.isFailure)
    }
  )
}
