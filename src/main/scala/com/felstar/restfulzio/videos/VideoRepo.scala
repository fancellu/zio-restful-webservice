package com.felstar.restfulzio.videos

import zio._
import zio.stream.ZStream

trait VideoRepo {
  def register(name: String): Task[String]

  def lookup(id: String): Task[Option[Video]]

  def videos: Task[List[Video]]

  def videosStream: ZStream[Any, Throwable, Video]
}

object VideoRepo {
  def register(name: String): ZIO[VideoRepo, Throwable, String] =
    ZIO.serviceWithZIO[VideoRepo](_.register(name))

  def lookup(id: String): ZIO[VideoRepo, Throwable, Option[Video]] = {
    ZIO.serviceWithZIO[VideoRepo](_.lookup(id))
  }

  def videos: ZIO[VideoRepo, Throwable, List[Video]] =
    ZIO.serviceWithZIO[VideoRepo](_.videos)

  def videosStream: ZStream[VideoRepo, Throwable, Video] =
    ZStream.serviceWithStream[VideoRepo](_.videosStream)
}
