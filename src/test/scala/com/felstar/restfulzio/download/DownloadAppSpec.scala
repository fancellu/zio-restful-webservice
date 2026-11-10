package com.felstar.restfulzio.download

import zio.http._
import zio.durationInt
import zio.test._

object DownloadAppSpec extends ZIOSpecDefault {

  private val app = DownloadApp()

  def spec = suite("DownloadAppSpec")(
    test("should download file.txt") {
      val req = Request.get("/download")

      for {
        expectedBody <- app.runZIO(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody.contains("end of file"))
    },
    test("should download bigfile.txt") {
      val req = Request.get("/download/stream")

      for {
        expectedBodyFiber <- app.runZIO(req).flatMap(_.body.asString).fork
        _ <- TestClock.adjust(10.minutes)
        expectedBody <- expectedBodyFiber.join
      } yield assertTrue(expectedBody.contains("100"))
    }
  )
}
