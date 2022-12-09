package com.felstar.restfulzio.openai

import zio.json._

import zio.ZIO
import zio.http._
import zio.http.model.{Header, Headers, Method}
import zio._

import com.felstar.openai.completion._
import SerDes._

// Modelled after the quickstart openai completion app https://github.com/openai/openai-quickstart-python.git

/** An http app that:
  *   - Accepts a `Request` and returns a `Response`
  *   - Could fail
  *   - Uses EventLoopGroup with ChannelFactory Env for Client
  */
object OpenAICompletionApp {

  val HOST = "https://api.openai.com/v1"

  def apply(): Http[
    Client,
    Throwable,
    Request,
    Response
  ] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "openai" / "superhero" / animal =>

        val url = s"$HOST/completions"
        val prompt=
          s"""Suggest three names for an animal that is a superhero.

        Animal: Cat
        Names: Captain Sharpclaw, Agent Fluffball, The Incredible Feline
        Animal: Dog
        Names: Ruff the Protector, Wonder Canine, Sir Barks-a-Lot
        Animal: ${animal.toUpperCase}
        Names:"""
        val json = for {
          open_api_key_property <- System.property("OPENAI_API_KEY")
          open_api_key_env <- System.env("OPENAI_API_KEY")
          open_api_key = open_api_key_property.orElse(open_api_key_env).getOrElse("ENTER_OPENAI_API_KEY")
          res <- Client.request(
            url,
            method = Method.POST,
            headers =
              Headers(Header("Content-type", "application/json; charset=UTF-8"), Header("Authorization", s"Bearer $open_api_key")),
            content = Body.fromString(
              CreateCompletionRequest(model="text-davinci-003", prompt= Some(prompt), temperature = Some(0.6)).toJson)
          )
          _ <- ZIO.logInfo(s"Called $url")
          string <- res.body.asString
          _ <- ZIO.logInfo(string)
          response = string.fromJson[CreateCompletionResponse].toOption
          json = response.map(_.toJsonPretty).getOrElse(string)
          // It can return an Error, especially if no valid OPEN_API_KEY
          // so we'd like to expose this to the user
          // e.g. "error": {
          //        "message": "Incorrect API key provided: ENTER_OP********_KEY. You can find your API key at https://beta.openai.com.",
          //        "type": "invalid_request_error",
          //        "param": null,
          //        "code": "invalid_api_key"
          //    }
        } yield json
        json.map(Response.json(_))


    }

}
