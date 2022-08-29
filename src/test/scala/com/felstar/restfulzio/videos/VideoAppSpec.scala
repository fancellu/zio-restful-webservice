package com.felstar.restfulzio.videos

import zhttp.http.Method.POST
import zhttp.http._
import zio.json.DecoderOps
import zio.test._

object VideoAppSpec extends ZIOSpecDefault {

  val app = VideoApp()

  def spec = suite("VideoAppSpec")(
    test("Loaded films are present") {
      for {
        _ <- app(Request(method=POST, url = URL(!! / "videos"/ "film1")))
        _ <- app(Request(url = URL(!! / "videos" / "loadup")))
        expectedBody <- app(Request(url = URL(!! / "videos"))).flatMap(_.body.asString)
        videos = expectedBody.fromJson[List[Video]].toOption
      } yield assertTrue(videos.exists(_.map(_.name).toSet == Set("one", "two", "three", "four", "film1")))
    }
  ).provide(InmemoryVideoRepo.layer)
}
