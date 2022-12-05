package com.felstar.restfulzio.openai
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

/**
 * @param prompt_tokens
 * @param completion_tokens
 * @param total_tokens
 */
case class CreateCompletionResponseUsage (
  prompt_tokens: Int,
  completion_tokens: Int,
  total_tokens: Int
)

object CreateCompletionResponseUsage {
  implicit val encoder: JsonEncoder[CreateCompletionResponseUsage] = DeriveJsonEncoder.gen[CreateCompletionResponseUsage]
  implicit val decoder: JsonDecoder[CreateCompletionResponseUsage] = DeriveJsonDecoder.gen[CreateCompletionResponseUsage]
}