package com.felstar.restfulzio.videos

import zio._
import zio.stream.ZStream

case class InmemoryVideoRepo(map: Ref[Map[String, Video]]) extends VideoRepo {
  override def register(name: String): UIO[String] =
    for {
      uuid <- Random.nextUUID
      id = uuid.toString
      _ <- map.update(_ + (id -> Video(name = name, uuid = uuid)))
    } yield id

  override def lookup(id: String): UIO[Option[Video]] =
    map.get.map(_.get(id))

  override def videos: UIO[List[Video]] =
    map.get.map(_.values.toList)

  override def videosStream: ZStream[Any, Throwable, Video] =
    ZStream.fromIterableZIO(videos)
}

object InmemoryVideoRepo {
  def layer: ZLayer[Any, Nothing, InmemoryVideoRepo] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[String, Video]).map(new InmemoryVideoRepo(_))
    )
}
