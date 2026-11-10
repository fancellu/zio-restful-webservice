package com.felstar.restfulzio.gemini

import com.felstar.restfulzio.gemini.GeminiModels._
import zio.ZIO
import zio.http._
import zio.json._

/** Routes that:
  *   - Accept a `Request` and return a `Response`
  *   - Could fail
  *   - Use a `Client`
  */
object GeminiTextApp {

  // Returns the generated text. Any error from Gemini (e.g. a bad or missing key) is passed on as json, with the upstream status
  def generate(prompt: String): ZIO[Client, Throwable, Response] =
    for {
      res <- generateContent(TEXT_MODEL, GenerateContentRequest.fromPrompt(prompt, temperature = Some(0.6)))
      string <- res.body.asString
      text = string.fromJson[GenerateContentResponse].toOption.flatMap(_.text)
    } yield text match {
      case Some(text) if !res.status.isError => Response.text(text)
      case _                                 => Response.json(string).status(res.status)
    }

  def apply(): Routes[Client, Throwable] = Routes(
    Method.GET / "gemini" / "prompt" / string("prompt") -> handler { (prompt: String, _: Request) =>
      generate(prompt)
    },
    Method.GET / "gemini" / "superhero" / string("animal") -> handler { (animal: String, _: Request) =>
      val prompt =
        s"""Suggest three names for an animal that is a superhero. Reply with just the names, comma separated.

      Animal: Cat
      Names: Captain Sharpclaw, Agent Fluffball, The Incredible Feline
      Animal: Dog
      Names: Ruff the Protector, Wonder Canine, Sir Barks-a-Lot
      Animal: ${animal.toUpperCase}
      Names:"""
      generate(prompt)
    }
  )

}
