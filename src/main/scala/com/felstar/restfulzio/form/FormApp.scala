package com.felstar.restfulzio.form

import zio._
import zio.http._

object FormApp {

  def apply(): Routes[Any, Nothing] = Routes(
    Method.POST / "handle_form" -> handler { (req: Request) =>
      for {
        stringIn <- req.body.asString.orDie
        _ <- ZIO.logInfo(stringIn)
      } yield Response.text(s"post is $stringIn")
    }
  )

}
