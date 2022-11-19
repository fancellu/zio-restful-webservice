package com.felstar.restfulzio.helloworld

import zio._
import zio.http._
import zio.http.model.Method

/**
 * An http app that: 
 *   - Accepts a `Request` and returns a `Response`
 *   - Does not fail
 *   - Uses a String for the env, for the webapp root
 */
object HelloWorldApp {
  def apply(): Http[String, Nothing, Request, Response] =
    Http.fromZIO(ZIO.service[String]).flatMap { root =>
      Http.collect[Request] {
        // GET /$root/?name=:name
        case req@Method.GET -> !! / `root` if req.url.queryParams.nonEmpty =>
          Response.text(s"Hello ${req.url.queryParams("name").mkString(" and ")}!")

        // GET /$root
        case Method.GET -> !! / `root` =>
          Response.text(s"Hello World!")

        // GET /$root/:name
        case Method.GET -> !! / `root` / name =>
          Response.text(s"Hello $name!")
      }
    }
}
