package com.felstar.restfulzio

import com.felstar.restfulzio.gemini.GeminiModels._
import zio.http._
import zio.json._
import zio.{ZIO, _}

package object gemini {
  val HOST = "https://generativelanguage.googleapis.com/v1beta/models"

  val TEXT_MODEL = "gemini-3.8-flash"
  val IMAGE_MODEL = "gemini-3.1-flash-image"

  val getGeminiAPIKey: Task[String] = for {
    property <- System.property("GEMINI_API_KEY")
    env <- System.env("GEMINI_API_KEY")
  } yield property.orElse(env).getOrElse("ENTER_GEMINI_API_KEY")

  // POSTs to the model's generateContent endpoint, with our key in the x-goog-api-key header (not in the url, so it is never logged)
  def generateContent(model: String, request: GenerateContentRequest): ZIO[Client, Throwable, Response] = {
    val url = s"$HOST/$model:generateContent"
    for {
      key <- getGeminiAPIKey
      res <- Client.batched(
        Request
          .post(url, Body.fromString(request.toJson))
          .addHeader(Header.ContentType(MediaType.application.json))
          .addHeader("x-goog-api-key", key)
      )
      _ <- ZIO.logInfo(s"Called $url, status ${res.status.code}")
    } yield res
  }
}
