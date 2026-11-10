package com.felstar.restfulzio.openai

import com.felstar.openai.moderation.{CreateModerationRequest, CreateModerationResponse}
import com.felstar.restfulzio.openai.SerDes._
import zio.ZIO
import zio.http._
import zio.json._

/** Routes that:
  *   - Accept a `Request` and return a `Response`
  *   - Could fail
  *   - Use a `Client`
  */
object OpenAIModerationApp {

  def moderation(input: String): ZIO[Client, Throwable, Response] ={
    val url = s"$HOST/moderations"
    for {
      res <- postJson(url, CreateModerationRequest(input = input).toJson)
      string <- res.body.asString
      _ <- ZIO.logInfo(string)
      response = string.fromJson[CreateModerationResponse].toOption
      json = response.map(_.toJsonPretty).getOrElse(string)
    } yield Response.json(json).status(res.status)
  }

  def apply(): Routes[Client, Throwable] = Routes(
    Method.GET / "openai" / "moderation" / string("input") -> handler { (input: String, _: Request) =>
      moderation(input)
    }
  )

}
