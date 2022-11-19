package com.felstar.restfulzio.download

import zio.stream.ZStream
import zio._
import zio.http._
import zio.http.model.{Header, Headers, Method}

import scala.concurrent.duration.DurationDouble

/**
 * An http app that: 
 *   - Accepts a `Request` and returns a `Response` 
 *   - May fail with type of `Throwable`
 *   - Does not require any environment
 */
object DownloadApp {
  def apply(): Http[Any, Throwable, Request, Response] =
    Http.collectHttp[Request] {
      // GET /download
      case Method.GET -> !! / "download" =>
        val fileName = "file.txt"
        Http.fromStream(ZStream.fromResource(fileName)).setHeaders(
          Headers(
            Header("Content-Type", "application/octet-stream"),
            Header("Content-Disposition", s"attachment; filename=$fileName")
          )
        )

      // Download a large file using streams, deliberately made to be slow
      // GET /download/stream
      case Method.GET -> !! / "download" / "stream" =>
        val file = "bigfile.txt"
        Http.fromStream(
          ZStream.fromResource(file)
            .schedule(Schedule.spaced(10.millis))
        ).setHeaders(
          Headers(
            Header("Content-Type", "application/octet-stream"),
            Header("Content-Disposition", s"attachment; filename=$file")
          )
        )
    }
}
