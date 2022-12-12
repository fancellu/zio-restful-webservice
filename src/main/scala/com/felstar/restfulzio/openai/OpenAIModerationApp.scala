package com.felstar.restfulzio.openai

import com.felstar.openai.completion._
import com.felstar.openai.moderation.{CreateModerationRequest, CreateModerationResponse}
import com.felstar.restfulzio.openai.SerDes._
import zio.ZIO
import zio.http._
import zio.http.model.{Header, Headers, Method}
import zio.json._

/** An http app that:
  *   - Accepts a `Request` and returns a `Response`
  *   - Could fail
  *   - Uses EventLoopGroup with ChannelFactory Env for Client
  */
object OpenAIModerationApp {

  def moderation(input: String): ZIO[Client, Throwable, Response] ={
    val url = s"$HOST/moderations"
    val json = for {
      open_api_key <- getOpenAPIKey
      res <- Client.request(
        url,
        method = Method.POST,
        headers =
          Headers(Header("Content-type", "application/json; charset=UTF-8"), Header("Authorization", s"Bearer $open_api_key")),
        content = Body.fromString(CreateModerationRequest(input = input).toJson)
      )
      _ <- ZIO.logInfo(s"Called $url")
      string <- res.body.asString
      _ <- ZIO.logInfo(string)
      response = string.fromJson[CreateModerationResponse].toOption
      json = response.map(_.toJsonPretty).getOrElse(string)
    } yield json
    json.map(Response.json(_))
  }

  def apply(): Http[
    Client,
    Throwable,
    Request,
    Response
  ] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "openai" / "moderation" / input =>
        moderation(input)
    }

}
