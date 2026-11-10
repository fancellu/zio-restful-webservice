package com.felstar.restfulzio.openai

import zio.json._
import zio.{ZIO, _}
import zio.http._
import com.felstar.openai.completion._
import SerDes._

// Modelled after the quickstart openai completion app https://github.com/openai/openai-quickstart-python.git

/** Routes that:
  *   - Accept a `Request` and return a `Response`
  *   - Could fail
  *   - Use a `Client`
  */
object OpenAICompletionApp {

  def completion(prompt: String): ZIO[Client, Throwable, Response] ={
    val url = s"$HOST/completions"
    for {
      res <- postJson(url,
          CreateCompletionRequest(model = "text-davinci-003", prompt = Some(prompt), temperature = Some(0.6)).toJson)
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
      // and we pass on the upstream status, so that an error is not reported as a 200
    } yield Response.json(json).status(res.status)
  }

  def apply(): Routes[Client, Throwable] = Routes(
    Method.GET / "openai" / "prompt" / string("prompt") -> handler { (prompt: String, _: Request) =>
      completion(prompt)
    },
    Method.GET / "openai" / "superhero" / string("animal") -> handler { (animal: String, _: Request) =>
      val prompt=
        s"""Suggest three names for an animal that is a superhero.

      Animal: Cat
      Names: Captain Sharpclaw, Agent Fluffball, The Incredible Feline
      Animal: Dog
      Names: Ruff the Protector, Wonder Canine, Sir Barks-a-Lot
      Animal: ${animal.toUpperCase}
      Names:"""
      completion(prompt)
    }
  )

}
