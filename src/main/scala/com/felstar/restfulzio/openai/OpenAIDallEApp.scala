package com.felstar.restfulzio.openai

import com.felstar.openai.image.{CreateImageRequest, ImagesResponse}
import zio.{ZIO, _}
import zio.http._
import zio.http.model.{Header, Headers, Method}
import zio.json._
import SerDes._
import zio.http.html._

object OpenAIDallEApp {
  def image(prompt: String): ZIO[Client, Throwable, Response] = {
    val url = s"$HOST/images/generations"
    for {
      open_api_key <- getOpenAPIKey
      res <- Client.request(
        url,
        method = Method.POST,
        headers =
          Headers(Header("Content-type", "application/json; charset=UTF-8"), Header("Authorization", s"Bearer $open_api_key")),
        content = Body.fromString(CreateImageRequest(prompt =prompt).toJson)
      )
      _ <- ZIO.logInfo(s"Called $url")
      string <- res.body.asString
      _ <- ZIO.logInfo(string)
      response = string.fromJson[ImagesResponse].toOption
      link: Option[String] = response.flatMap(_.data.headOption.flatMap(_.url))
      element = link.map(url=>img(srcAttr:= url)).getOrElse(div("Image not found"))
    } yield Response.html(element)
  }

  def apply(): Http[
    Client,
    Throwable,
    Request,
    Response
  ] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "openai" / "dalle" / prompt =>
        image(prompt)
    }

}
