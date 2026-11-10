package com.felstar.restfulzio.gemini

import com.felstar.restfulzio.gemini.GeminiModels._
import zio.ZIO
import zio.http._
import zio.http.template._
import zio.json._

/** Routes that:
  *   - Accept a `Request` and return a `Response`
  *   - Could fail
  *   - Use a `Client`
  */
object GeminiImageApp {

  // Gemini returns the image itself, base64 encoded, so we embed it in the page as a data: url
  def image(prompt: String): ZIO[Client, Throwable, Response] =
    for {
      res <- generateContent(IMAGE_MODEL, GenerateContentRequest.fromPrompt(prompt))
      string <- res.body.asString
      data = string.fromJson[GenerateContentResponse].toOption.flatMap(_.inlineData)
      _ <- ZIO.when(data.isEmpty)(ZIO.logWarning(s"No image returned: $string"))
      element: Html = data
        .map(d => img(srcAttr := s"data:${d.mimeType};base64,${d.data}", altAttr := prompt): Html)
        .getOrElse(div("Image not found"))
    } yield Response.html(element, res.status)

  def apply(): Routes[Client, Throwable] = Routes(
    Method.GET / "gemini" / "image" / string("prompt") -> handler { (prompt: String, _: Request) =>
      image(prompt)
    }
  )

}
