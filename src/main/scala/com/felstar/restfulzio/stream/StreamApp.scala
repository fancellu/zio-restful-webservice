package com.felstar.restfulzio.stream

import com.felstar.restfulzio.videos.VideoRepo
import zhttp.http._
import zio._
import zio.stream.ZStream
import com.felstar.restfulzio.videos.Video._
import zio.json._


/** An http app that:
  *   - Accepts a `Request` and returns a `Response`
  *   - May fail with type of `Throwable`
  *   - Requires a VideoRepo
  */
object StreamApp {

  val randomInt: ZStream[Any, Nothing, String] = ZStream.repeatZIO(Random.nextInt).intersperse(999).map(r=>s"$r\n").take(10).schedule(Schedule.spaced(50.millis))

  val incrementing: ZStream[Any, Nothing, String] = ZStream.range(0, 50).map(i => s"$i\n").schedule(Schedule.spaced(100.millis))

  val headers= Headers(
    ("Content-Type", "application/octet-stream"),
    ("Content-Disposition", s"attachment; filename=stream.txt")
  )

  val videos: ZStream[VideoRepo, Throwable, String] = {
    VideoRepo.videosStream.map(response => response.toJson).schedule(Schedule.spaced(500.millis))
  }

  def apply(): Http[VideoRepo, Throwable, Request, Response] =

    Http.collectHttp[Request] {

      case Method.GET -> !! / "stream" / "incrementing" =>
        Http
          .fromStream(incrementing)
          .setHeaders(headers)
      case Method.GET -> !! / "stream" / "randomInt" =>
        Http
          .fromStream(randomInt)
          .setHeaders(headers)
      case Method.GET -> !! / "stream" / "both" =>
        Http
          .fromStream(incrementing ++ randomInt)
          .setHeaders(headers)
      case Method.GET -> !! / "stream" / "videos" =>
        Http
          .fromStream(videos)
          .setHeaders(headers)
    }
}
