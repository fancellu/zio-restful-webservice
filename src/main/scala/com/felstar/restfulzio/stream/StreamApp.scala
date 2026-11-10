package com.felstar.restfulzio.stream

import com.felstar.restfulzio.videos.VideoRepo
import zio.http._
import zio._
import zio.stream.ZStream
import com.felstar.restfulzio.videos.Video._
import zio.json._


/** Routes that:
  *   - Accept a `Request` and return a `Response`
  *   - Do not fail (though the stream in the body could)
  *   - Require a VideoRepo
  */
object StreamApp {

  val randomInt: ZStream[Any, Nothing, String] = ZStream.repeatZIO(Random.nextInt).intersperse(999).map(r=>s"$r\n").take(10).schedule(Schedule.spaced(50.millis))

  val incrementing: ZStream[Any, Nothing, String] = ZStream.range(0, 50).map(i => s"$i\n").schedule(Schedule.spaced(100.millis))

  private val headers= Headers(
    Header.ContentType(MediaType.application.`octet-stream`),
    Header.ContentDisposition.attachment("stream.txt")
  )

  // newline delimited json
  val videos: ZStream[VideoRepo, Throwable, String] = {
    VideoRepo.videosStream.map(response => response.toJson + "\n").schedule(Schedule.spaced(500.millis))
  }

  private def streamed(stream: ZStream[Any, Throwable, String]): Response =
    Response(headers = headers, body = Body.fromCharSequenceStreamChunked(stream))

  def apply(): Routes[VideoRepo, Nothing] = Routes(
    Method.GET / "stream" / "incrementing" -> handler(streamed(incrementing)),
    Method.GET / "stream" / "randomInt" -> handler(streamed(randomInt)),
    Method.GET / "stream" / "both" -> handler(streamed(incrementing ++ randomInt)),
    // The body outlives the handler, so we give the stream its environment up front
    Method.GET / "stream" / "videos" -> handler {
      ZIO.environmentWith[VideoRepo](env => streamed(videos.provideEnvironment(env)))
    }
  )
}
