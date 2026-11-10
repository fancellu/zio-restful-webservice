package com.felstar.restfulzio.staticserver

import zio.http._
import zio.test._

object StaticAppSpec extends ZIOSpecDefault {

  // sandbox turns failures into 500 responses, as Routes can only be run once their errors are handled
  private val app = StaticApp().sandbox

  def spec = suite("StaticAppSpec")(
    test("should serve a file from the static dir") {
      for {
        body <- app.runZIO(Request.get("/test.txt")).flatMap(_.body.asString)
      } yield assertTrue(body.contains("Hello from static dir file test.txt"))
    },
    test("should list a sub directory") {
      for {
        body <- app.runZIO(Request.get("/subdir")).flatMap(_.body.asString)
      } yield assertTrue(body.contains("test3.txt"))
    },
    test("should not serve files outside the static dir") {
      // Built from a Path, so that nothing normalises the ".." away before it gets to the routes
      val req = Request.get(URL(Path.root / ".." / "build.sbt"))

      for {
        response <- app.runZIO(req)
        body     <- response.body.asString
      } yield assertTrue(response.status == Status.NotFound, !body.contains("scalaVersion"))
    },
    test("should not list directories outside the static dir") {
      val req = Request.get(URL(Path.root / ".." / ".."))

      for {
        response <- app.runZIO(req)
      } yield assertTrue(response.status == Status.NotFound)
    }
  )
}
