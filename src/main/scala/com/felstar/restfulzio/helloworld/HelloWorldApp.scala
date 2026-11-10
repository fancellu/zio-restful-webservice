package com.felstar.restfulzio.helloworld

import zio._
import zio.http._

/**
 * Routes that:
 *   - Accept a `Request` and return a `Response`
 *   - Do not fail
 *   - Use a String from the env, for the webapp root
 */
object HelloWorldApp {

  def routes(root: String): Routes[Any, Nothing] = Routes(
    // GET /$root and GET /$root?name=:name
    Method.GET / root -> handler { (req: Request) =>
      val names = req.queryParams("name")
      if (names.nonEmpty) Response.text(s"Hello ${names.mkString(" and ")}!")
      else Response.text(s"Hello World!")
    },
    // GET /$root/:name
    Method.GET / root / string("name") -> handler { (name: String, _: Request) =>
      Response.text(s"Hello $name!")
    }
  )

  // Routes are values, so we can build them from what we find in the environment. No root, no routes
  def apply(): URIO[Map[String, String], Routes[Any, Nothing]] =
    ZIO.serviceAt[String]("helloworld").map(_.map(routes).getOrElse(Routes.empty))
}
