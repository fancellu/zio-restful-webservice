package com.felstar.restfulzio.noenv

import zhttp.http._
import zio.ZIO

/**
 * An http app that:
 *   - Accepts a `Request` and returns a `Response`
 *   - Does not fail
 *   - Does not use Env
 */
object NoEnvApp {
  def apply(): Http[Any, Nothing, Request, Response] =

      Http.collect[Request] {
        // GET /noenv
        case Method.GET -> !! / "noenv" => Response.text(s"Hello from noenv")
        case req @ Method.GET -> !! / "headers" => Response.text(s"""headers are ${req.headers.toList.mkString("\n")}""")
      }

}
