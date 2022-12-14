package com.felstar.restfulzio.videos

import io.getquill.jdbczio.Quill
import io.getquill.{Escape, H2ZioJdbcContext}
import zio._
import zio.stream.ZStream

import java.util.UUID
import javax.sql.DataSource

case class VideoTable(uuid: UUID, name: String)

case class PersistentVideoRepo(ds: DataSource) extends VideoRepo {
  val ctx = new H2ZioJdbcContext(Escape)

  import ctx._

  override def register(name: String): Task[String] = {
    for {
      id <- Random.nextUUID
      _ <- ctx.run {
        quote {
          query[VideoTable].insertValue {
            lift(VideoTable(id, name))
          }
        }
      }
    } yield id.toString
  }.provide(ZLayer.succeed(ds))

  override def lookup(id: String): Task[Option[Video]] = {
    if (id.isEmpty) ZIO.succeed(None) else
    ctx.run {
      quote {
        query[VideoTable]
          .filter(p => p.uuid == lift(UUID.fromString(id)))
          .map(v => Video(v.name, v.uuid))
      }
    }.provide(ZLayer.succeed(ds)).map(_.headOption)
  }

  override def videos: Task[List[Video]] =
    ctx.run {
      quote {
        query[VideoTable].map(v => Video(v.name, v.uuid ))
      }
    }.provide(ZLayer.succeed(ds))

  override def videosStream: ZStream[Any, Throwable, Video] =
    ctx.stream {
      quote {
        query[VideoTable].map(v => Video(v.name, v.uuid))
      }
    }.provideLayer(ZLayer.succeed(ds))
}

object PersistentVideoRepo {
  def layer: ZLayer[Any, Throwable, PersistentVideoRepo] =
    Quill.DataSource.fromPrefix("VideoApp") >>>
      ZLayer.fromFunction(PersistentVideoRepo(_))
}
