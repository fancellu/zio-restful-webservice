package com.felstar.restfulzio.openai

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import com.felstar.openai.completion._

object SerDes {
  implicit val encoder4: JsonEncoder[CreateCompletionResponseLogprobs] = DeriveJsonEncoder.gen[CreateCompletionResponseLogprobs]
  implicit val decoder4: JsonDecoder[CreateCompletionResponseLogprobs] = DeriveJsonDecoder.gen[CreateCompletionResponseLogprobs]

  implicit val encoder3: JsonEncoder[CreateCompletionResponseChoices] = DeriveJsonEncoder.gen[CreateCompletionResponseChoices]
  implicit val decoder3: JsonDecoder[CreateCompletionResponseChoices] = DeriveJsonDecoder.gen[CreateCompletionResponseChoices]

  implicit val encoder1: JsonEncoder[CreateCompletionRequest] = DeriveJsonEncoder.gen[CreateCompletionRequest]
  implicit val decoder1: JsonDecoder[CreateCompletionRequest] = DeriveJsonDecoder.gen[CreateCompletionRequest]

  implicit val encoder5: JsonEncoder[CreateCompletionResponseUsage] = DeriveJsonEncoder.gen[CreateCompletionResponseUsage]
  implicit val decoder5: JsonDecoder[CreateCompletionResponseUsage] = DeriveJsonDecoder.gen[CreateCompletionResponseUsage]

  implicit val encoder2: JsonEncoder[CreateCompletionResponse] = DeriveJsonEncoder.gen[CreateCompletionResponse]
  implicit val decoder2: JsonDecoder[CreateCompletionResponse] = DeriveJsonDecoder.gen[CreateCompletionResponse]
}
