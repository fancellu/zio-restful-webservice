package com.felstar.restfulzio.videos

import zio.http._
import zio.json.DecoderOps
import zio.{Scope, ZIO}
import zio.test._

object VideoAppSpec extends ZIOSpecDefault {

  // sandbox turns failures into 500 responses, as Routes can only be run once their errors are handled
  val app = VideoApp().sandbox

  def spec = suite("VideoAppSpec")(
    test("Loaded films are present") {
      for {
        _ <- app.runZIO(Request.post("/videos/film1", Body.empty))
        _ <- app.runZIO(Request.get("/videos/loadup"))
        expectedBody <- app.runZIO(Request.get("/videos")).flatMap(_.body.asString)
        videos = expectedBody.fromJson[List[Video]].toOption
      } yield assertTrue(videos.exists(_.map(_.name).toSet == Set("one", "two", "three", "four", "film1")))
    },
    test("A registered film can be looked up, an unknown one is a 404") {
      for {
        id      <- app.runZIO(Request.post("/videos/film1", Body.empty)).flatMap(_.body.asString)
        found   <- app.runZIO(Request.get(s"/videos/$id")).flatMap(_.body.asString)
        missing <- app.runZIO(Request.get("/videos/abc"))
      } yield assertTrue(found.contains("film1"), missing.status == Status.NotFound)
    },
    test("Concurrent registrations are not lost") {
      for {
        _ <- ZIO.foreachParDiscard(1 to 1000)(i => VideoRepo.register(s"film$i"))
        videos <- VideoRepo.videos
      } yield assertTrue(videos.size == 1000)
    }
  ).provide(InmemoryVideoRepo.layer, Scope.default)
}
