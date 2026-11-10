package com.felstar.restfulzio.videos

import zio.http.{Body, _}
import zio.http.model.Method.POST
import zio.json.DecoderOps
import zio.ZIO
import zio.test._

object VideoAppSpec extends ZIOSpecDefault {

  val app = VideoApp()

  def spec = suite("VideoAppSpec")(
    test("Loaded films are present") {
      for {
        _ <- app(Request.post(body = Body.empty, url = URL(!! / "videos"/ "film1")))
        _ <- app(Request.get(url = URL(!! / "videos" / "loadup")))
        expectedBody <- app(Request.get(url = URL(!! / "videos"))).flatMap(_.body.asString)
        videos = expectedBody.fromJson[List[Video]].toOption
      } yield assertTrue(videos.exists(_.map(_.name).toSet == Set("one", "two", "three", "four", "film1")))
    },
    test("Concurrent registrations are not lost") {
      for {
        _ <- ZIO.foreachParDiscard(1 to 1000)(i => VideoRepo.register(s"film$i"))
        videos <- VideoRepo.videos
      } yield assertTrue(videos.size == 1000)
    }
  ).provide(InmemoryVideoRepo.layer)
}
