package com.felstar.restfulzio.hellotwirl

import html.hellotwirl
import zio.http.{html, _}
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues}
import zio.http.model.{Headers, Method}

/** An http app that:
  *   - Accepts a `Request` and returns a `Response`
  *   - Does not fail
  *   - Does not use Env
  */
object HelloTwirlApp {

  val textHTML: Headers =
    Headers(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML)

  def apply(): Http[Any, Nothing, Request, Response] =
    Http.collect[Request] {
      // GET /hellotwirl/:string
      case Method.GET -> !! / "hellotwirl" / string  =>
        Response(
          body = Body.fromString(hellotwirl.render(string).toString),
          headers = textHTML
        )
      // GET /hellotwirl
      case Method.GET -> !! / "hellotwirl" =>
        Response(
          body = Body.fromString(hellotwirl.render("defaultstring").toString),
          headers = textHTML
        )
    }

}
