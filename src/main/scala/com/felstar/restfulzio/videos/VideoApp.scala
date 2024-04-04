package com.felstar.restfulzio.videos

import zio.http._
import zio._
import zio.http.model.{Method, Status}
import zio.json._

import java.util.UUID

/** An http app that:
  *   - Accepts a `Request` and returns a `Response`
  *   - May fail with type of `Throwable`
  *   - Uses a `VideoRepo` as the environment
  */
object VideoApp {
  def apply(): Http[VideoRepo, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // POST /videos/:name
      case Method.POST -> !! / "videos" / name =>
              VideoRepo.register(name).map(uuiid => Response.text(uuiid))

      case Method.GET -> !! / "videos" / "loadup" =>
        val names = List("one", "two", "three", "four")

        val out: ZIO[VideoRepo, Throwable, List[String]] =
          ZIO.foreach(names)(name => VideoRepo.register(name))

        out.map(ids => Response.text("bulk load: " + ids.mkString(",")))

      // GET /videos/:id
      case Method.GET -> !! / "videos" / id =>
        VideoRepo
          .lookup(id)
          .map {
            case Some(user) =>
              Response.json(user.toJson)
            case None =>
              Response.status(Status.NotFound)
          }
      // GET /videos
      case Method.GET -> !! / "videos" =>
        VideoRepo.videos.map(response => Response.json(response.toJson))
    }

}
