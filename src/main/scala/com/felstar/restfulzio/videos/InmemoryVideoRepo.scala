package com.felstar.restfulzio.videos

import zio._
import zio.stream.ZStream

import java.util.UUID
import scala.collection.mutable

  case class InmemoryVideoRepo(map: Ref[mutable.Map[String, Video]]) extends VideoRepo {
    override def register(name: String): UIO[String] =
      for {
        id <- Random.nextUUID.map(_.toString)
        _ <- map.updateAndGet(_ addOne(id, Video(name=name, uuid=UUID.fromString(id))))
      } yield id

    override def lookup(id: String): UIO[Option[Video]] =
      map.get.map(_.get(id))

    override def videos: UIO[List[Video]] =
       map.get.map(_.values.toList)

    override def videosStream: ZStream[Any, Throwable, Video] = {
      ZStream.fromIterableZIO(map.get.map(_.values.toList))
    }
  }

  object InmemoryVideoRepo {
    def layer: ZLayer[Any, Nothing, InmemoryVideoRepo] =
      ZLayer.fromZIO(
        Ref.make(mutable.Map.empty[String, Video]).map(new InmemoryVideoRepo(_))
      )
  }