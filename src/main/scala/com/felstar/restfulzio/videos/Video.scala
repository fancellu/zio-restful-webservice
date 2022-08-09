package com.felstar.restfulzio.videos

import zio.json._

import java.util.UUID

case class Video(name: String, uuid: UUID)

object Video {
  implicit val encoder: JsonEncoder[Video] =
    DeriveJsonEncoder.gen[Video]
  implicit val decoder: JsonDecoder[Video] =
    DeriveJsonDecoder.gen[Video]
}
