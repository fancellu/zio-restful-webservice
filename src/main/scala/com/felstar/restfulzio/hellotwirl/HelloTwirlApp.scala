package com.felstar.restfulzio.hellotwirl

import zhttp.http._
import html.hellotwirl

import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues}

/** An http app that:
  *   - Accepts a `Request` and returns a `Response`
  *   - Does not fail
  *   - Does not use Env
  */
object HelloTwirlApp {

  val textHTML: Headers =
    Headers(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML)

  def apply(): Http[String, Nothing, Request, Response] =
    Http.collect[Request] {
      // GET /hellotwirl/:string
      case Method.GET -> !! / "hellotwirl" / string  =>
        Response(
          data = HttpData.fromString(hellotwirl.render(string).toString),
          headers = textHTML
        )
      // GET /hellotwirl
      case Method.GET -> !! / "hellotwirl" =>
        Response(
          data = HttpData.fromString(hellotwirl.render("defaultstring").toString),
          headers = textHTML
        )
    }

}
