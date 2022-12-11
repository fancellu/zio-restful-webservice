package com.felstar.restfulzio.openai

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import com.felstar.openai.completion._
import com.felstar.openai.image._

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

  implicit val encoder8: JsonEncoder[ImagesResponseData] = DeriveJsonEncoder.gen[ImagesResponseData]
  implicit val decoder8: JsonDecoder[ImagesResponseData] = DeriveJsonDecoder.gen[ImagesResponseData]

  implicit val encoder7: JsonEncoder[ImagesResponse] = DeriveJsonEncoder.gen[ImagesResponse]
  implicit val decoder7: JsonDecoder[ImagesResponse] = DeriveJsonDecoder.gen[ImagesResponse]

  implicit val encoder6: JsonEncoder[CreateImageRequest] = DeriveJsonEncoder.gen[CreateImageRequest]
  implicit val decoder6: JsonDecoder[CreateImageRequest] = DeriveJsonDecoder.gen[CreateImageRequest]
}
