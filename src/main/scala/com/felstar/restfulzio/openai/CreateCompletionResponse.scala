package com.felstar.restfulzio.openai

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

/**
 * @param id 
 * @param `object` 
 * @param created 
 * @param model 
 * @param choices 
 * @param usage 
 */
case class CreateCompletionResponse (
  id: String,
  `object`: String,
  created: Int,
  model: String,
  choices: List[CreateCompletionResponseChoices],
  usage: Option[CreateCompletionResponseUsage] = None
)

object CreateCompletionResponse {
  implicit val encoder: JsonEncoder[CreateCompletionResponse] = DeriveJsonEncoder.gen[CreateCompletionResponse]
  implicit val decoder: JsonDecoder[CreateCompletionResponse] = DeriveJsonDecoder.gen[CreateCompletionResponse]
}
