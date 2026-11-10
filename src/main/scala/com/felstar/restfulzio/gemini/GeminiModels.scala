package com.felstar.restfulzio.gemini

import zio.json._

// Just the parts of the Gemini generateContent API that we use, unknown fields are ignored when decoding
// https://ai.google.dev/api/generate-content

final case class InlineData(mimeType: String, data: String)

final case class Part(text: Option[String] = None, inlineData: Option[InlineData] = None)

final case class Content(parts: List[Part], role: Option[String] = None)

final case class GenerationConfig(temperature: Option[Double] = None)

final case class GenerateContentRequest(contents: List[Content], generationConfig: Option[GenerationConfig] = None)

object GenerateContentRequest {
  def fromPrompt(prompt: String, temperature: Option[Double] = None): GenerateContentRequest =
    GenerateContentRequest(
      contents = List(Content(parts = List(Part(text = Some(prompt))))),
      generationConfig = temperature.map(t => GenerationConfig(temperature = Some(t)))
    )
}

final case class Candidate(content: Option[Content] = None, finishReason: Option[String] = None)

final case class GenerateContentResponse(candidates: List[Candidate] = Nil) {
  private def parts: List[Part] = candidates.headOption.flatMap(_.content).map(_.parts).getOrElse(Nil)

  // All the text parts of the first candidate, joined
  def text: Option[String] = Some(parts.flatMap(_.text).mkString).filter(_.nonEmpty)

  // The first image (or other binary) part of the first candidate
  def inlineData: Option[InlineData] = parts.collectFirst { case Part(_, Some(data)) => data }
}

object GeminiModels {
  implicit val inlineDataCodec: JsonCodec[InlineData] = DeriveJsonCodec.gen[InlineData]
  implicit val partCodec: JsonCodec[Part] = DeriveJsonCodec.gen[Part]
  implicit val contentCodec: JsonCodec[Content] = DeriveJsonCodec.gen[Content]
  implicit val generationConfigCodec: JsonCodec[GenerationConfig] = DeriveJsonCodec.gen[GenerationConfig]
  implicit val requestCodec: JsonCodec[GenerateContentRequest] = DeriveJsonCodec.gen[GenerateContentRequest]
  implicit val candidateCodec: JsonCodec[Candidate] = DeriveJsonCodec.gen[Candidate]
  implicit val responseCodec: JsonCodec[GenerateContentResponse] = DeriveJsonCodec.gen[GenerateContentResponse]
}
