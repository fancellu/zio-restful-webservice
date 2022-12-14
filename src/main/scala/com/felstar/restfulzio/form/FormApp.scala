package com.felstar.restfulzio.form

import zio._
import zio.http._
import zio.http.model.Method

object FormApp {

  // As of now, we don't have multipart support, coming soon!
  def apply(): Http[Any, Nothing, Request, Response] = {
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "handle_form" =>
        for {
          stringIn <- req.body.asString.orDie
          _ <- ZIO.logInfo(stringIn)
        } yield Response.text(s"post is $stringIn")
    }
  }

}
