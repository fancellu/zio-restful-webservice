package com.felstar.restfulzio.openai

import com.felstar.openai.image.{CreateImageRequest, ImagesResponse}
import zio.{ZIO, _}
import zio.http._
import zio.json._
import SerDes._
import zio.http.template._

object OpenAIDallEApp {
  def image(prompt: String): ZIO[Client, Throwable, Response] = {
    val url = s"$HOST/images/generations"
    for {
      res <- postJson(url, CreateImageRequest(prompt =prompt).toJson)
      string <- res.body.asString
      _ <- ZIO.logInfo(string)
      response = string.fromJson[ImagesResponse].toOption
      link: Option[String] = response.flatMap(_.data.headOption.flatMap(_.url))
      element: Html = link.map(url=>img(srcAttr:= url): Html).getOrElse(div("Image not found"))
    } yield Response.html(element, res.status)
  }

  def apply(): Routes[Client, Throwable] = Routes(
    Method.GET / "openai" / "dalle" / string("prompt") -> handler { (prompt: String, _: Request) =>
      image(prompt)
    }
  )

}
