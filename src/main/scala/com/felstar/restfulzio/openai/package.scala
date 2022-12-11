package com.felstar.restfulzio

import zio.{ZIO, _}

package object openai {
  val HOST = "https://api.openai.com/v1"

  val getOpenAPIKey: Task[String] = for {
    open_api_key_property <- System.property("OPENAI_API_KEY")
    open_api_key_env <- System.env("OPENAI_API_KEY")
    open_api_key = open_api_key_property.orElse(open_api_key_env).getOrElse("ENTER_OPENAI_API_KEY")
  } yield open_api_key
}
