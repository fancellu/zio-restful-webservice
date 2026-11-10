package com.felstar.restfulzio.openai

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import com.felstar.openai.moderation.{CreateModerationRequest, CreateModerationResponse, CreateModerationResponseCategories, CreateModerationResponseCategoryScores, CreateModerationResponseResults}

object SerDes {
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
