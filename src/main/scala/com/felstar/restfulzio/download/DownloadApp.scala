package com.felstar.restfulzio.download

import zio.stream.ZStream
import zio._
import zio.http._

/**
 * Routes that:
 *   - Accept a `Request` and return a `Response`
 *   - Do not fail (though the stream in the body could)
 *   - Do not require any environment
 */
object DownloadApp {

  private def download(fileName: String, stream: ZStream[Any, Throwable, Byte]): Response =
    Response(
      headers = Headers(
        Header.ContentType(MediaType.application.`octet-stream`),
        Header.ContentDisposition.attachment(fileName)
      ),
      body = Body.fromStreamChunked(stream)
    )

  def apply(): Routes[Any, Nothing] = Routes(
    // GET /download
    Method.GET / "download" -> handler {
      val fileName = "file.txt"
      download(fileName, ZStream.fromResource(fileName))
    },
    // Download a large file using streams, deliberately made to be slow
    // GET /download/stream
    Method.GET / "download" / "stream" -> handler {
      val file = "bigfile.txt"
      download(file, ZStream.fromResource(file).schedule(Schedule.spaced(10.millis)))
    }
  )
}
