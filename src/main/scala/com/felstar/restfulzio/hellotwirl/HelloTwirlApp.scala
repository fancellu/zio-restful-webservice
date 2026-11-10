package com.felstar.restfulzio.hellotwirl

import _root_.html.hellotwirl
import zio.http._

/** Routes that:
  *   - Accept a `Request` and return a `Response`
  *   - Do not fail
  *   - Do not use Env
  */
object HelloTwirlApp {

  val textHTML: Headers =
    Headers(Header.ContentType(MediaType.text.html))

  private def render(param: String): Response =
    Response(
      body = Body.fromString(hellotwirl.render(param).toString),
      headers = textHTML
    )

  def apply(): Routes[Any, Nothing] = Routes(
    // GET /hellotwirl/:string
    Method.GET / "hellotwirl" / string("string") -> handler { (string: String, _: Request) =>
      render(string)
    },
    // GET /hellotwirl
    Method.GET / "hellotwirl" -> handler(render("defaultstring"))
  )

}
