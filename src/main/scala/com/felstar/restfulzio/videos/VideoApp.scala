package com.felstar.restfulzio.videos

import zio.http._
import zio._
import zio.json._

/** Routes that:
  *   - Accept a `Request` and return a `Response`
  *   - May fail with type of `Throwable`
  *   - Use a `VideoRepo` as the environment
  */
object VideoApp {
  def apply(): Routes[VideoRepo, Throwable] = Routes(
    // POST /videos/:name
    Method.POST / "videos" / string("name") -> handler { (name: String, _: Request) =>
      VideoRepo.register(name).map(uuiid => Response.text(uuiid))
    },
    Method.GET / "videos" / "loadup" -> handler {
      val names = List("one", "two", "three", "four")

      val out: ZIO[VideoRepo, Throwable, List[String]] =
        ZIO.foreach(names)(name => VideoRepo.register(name))

      out.map(ids => Response.text("bulk load: " + ids.mkString(",")))
    },
    // GET /videos/:id
    Method.GET / "videos" / string("id") -> handler { (id: String, _: Request) =>
      VideoRepo
        .lookup(id)
        .map {
          case Some(user) =>
            Response.json(user.toJson)
          case None =>
            Response.status(Status.NotFound)
        }
    },
    // GET /videos
    Method.GET / "videos" -> handler {
      VideoRepo.videos.map(response => Response.json(response.toJson))
    }
  )

}
