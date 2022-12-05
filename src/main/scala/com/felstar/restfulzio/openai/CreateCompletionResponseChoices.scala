package com.felstar.restfulzio.openai

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

/**
 * @param text 
 * @param index 
 * @param logprobs 
 * @param finish_reason 
 */
case class CreateCompletionResponseChoices (
  text: Option[String] = None,
  index: Option[Int] = None,
  logprobs: Option[CreateCompletionResponseLogprobs] = None,
  finish_reason: Option[String] = None
)

object CreateCompletionResponseChoices {
  implicit val encoder: JsonEncoder[CreateCompletionResponseChoices] = DeriveJsonEncoder.gen[CreateCompletionResponseChoices]
  implicit val decoder: JsonDecoder[CreateCompletionResponseChoices] = DeriveJsonDecoder.gen[CreateCompletionResponseChoices]
}
