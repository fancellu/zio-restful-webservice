package com.felstar.restfulzio.openai

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import com.felstar.openai.completion._
import com.felstar.openai.image._
import com.felstar.openai.moderation.{CreateModerationRequest, CreateModerationResponse, CreateModerationResponseCategories, CreateModerationResponseCategoryScores, CreateModerationResponseResults}

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

  implicit val encoder12: JsonEncoder[CreateModerationResponseCategories] = DeriveJsonEncoder.gen[CreateModerationResponseCategories]
  implicit val decode12: JsonDecoder[CreateModerationResponseCategories] = DeriveJsonDecoder.gen[CreateModerationResponseCategories]

  implicit val encoder13: JsonEncoder[CreateModerationResponseCategoryScores] = DeriveJsonEncoder.gen[CreateModerationResponseCategoryScores]
  implicit val decode13: JsonDecoder[CreateModerationResponseCategoryScores] = DeriveJsonDecoder.gen[CreateModerationResponseCategoryScores]

  implicit val encoder11: JsonEncoder[CreateModerationResponseResults] = DeriveJsonEncoder.gen[CreateModerationResponseResults]
  implicit val decode11: JsonDecoder[CreateModerationResponseResults] = DeriveJsonDecoder.gen[CreateModerationResponseResults]

  implicit val encoder9: JsonEncoder[CreateModerationResponse] = DeriveJsonEncoder.gen[CreateModerationResponse]
  implicit val decoder9: JsonDecoder[CreateModerationResponse] = DeriveJsonDecoder.gen[CreateModerationResponse]

  implicit val encoder10: JsonEncoder[CreateModerationRequest] = DeriveJsonEncoder.gen[CreateModerationRequest]
  implicit val decoder10: JsonDecoder[CreateModerationRequest] = DeriveJsonDecoder.gen[CreateModerationRequest]
}
