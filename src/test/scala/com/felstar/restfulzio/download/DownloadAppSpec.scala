package com.felstar.restfulzio.download

import zio.http._
import zio.Clock.currentTime
import zio.Console.print
import zio.Console.printLine
import zio.durationInt
import zio.test._

import java.util.concurrent.TimeUnit

object DownloadAppSpec extends ZIOSpecDefault {

  private val app = DownloadApp()

  def spec = suite("DownloadAppSpec")(
    test("should download file.txt") {
      val path = !! / "download"
      val req = Request.get(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody.contains("end of file"))
    },
    test("should download bigfile.txt") {
      val path = !! / "download" / "stream"
      val req = Request.get(url = URL(path))

      for {
        expectedBodyFiber <- app(req).flatMap(_.body.asString).fork
        _ <- TestClock.adjust(10.minutes)
        expectedBody <- expectedBodyFiber.join
      } yield assertTrue(expectedBody.contains("100"))
    }
  )
}
