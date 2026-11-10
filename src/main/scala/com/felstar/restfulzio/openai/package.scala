package com.felstar.restfulzio

import zio.http._
import zio.{ZIO, _}

package object openai {
  val HOST = "https://api.openai.com/v1"

  val getOpenAPIKey: Task[String] = for {
    open_api_key_property <- System.property("OPENAI_API_KEY")
    open_api_key_env <- System.env("OPENAI_API_KEY")
    open_api_key = open_api_key_property.orElse(open_api_key_env).getOrElse("ENTER_OPENAI_API_KEY")
  } yield open_api_key

  // POSTs json to OpenAI, with our key as the bearer token
  def postJson(url: String, json: String): ZIO[Client, Throwable, Response] = for {
    open_api_key <- getOpenAPIKey
    res <- Client.batched(
      Request
        .post(url, Body.fromString(json))
        .addHeader(Header.ContentType(MediaType.application.json))
        .addHeader(Header.Authorization.Bearer(open_api_key))
    )
    _ <- ZIO.logInfo(s"Called $url")
  } yield res
}
