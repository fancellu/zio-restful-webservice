package com.felstar.restfulzio.client

import zio.ZIO
import zio.cache.Cache
import zio.http._
import zio.http.model.{Headers, Method}
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.{Files, Paths}
import scala.util.Try

case class Post(userId: Int, id: Int, title: String, body: String)

object Post {
  implicit val encoder: JsonEncoder[Post] =
    DeriveJsonEncoder.gen[Post]
  implicit val decoder: JsonDecoder[Post] =
    DeriveJsonDecoder.gen[Post]
}

case class AnimeRequest(exclude: String)

object AnimeRequest {
  implicit val encoder: JsonEncoder[AnimeRequest] = DeriveJsonEncoder.gen[AnimeRequest]
}

case class AnimeResponse(files: Array[String])

object AnimeResponse {
  implicit val decoder: JsonDecoder[AnimeResponse] = DeriveJsonDecoder.gen[AnimeResponse]
  implicit val encoder: JsonEncoder[AnimeResponse] = DeriveJsonEncoder.gen[AnimeResponse]
}

/** An http app that:
  *   - Accepts a `Request` and returns a `Response`
  *   - Could fail
  *   - Uses EventLoopGroup with ChannelFactory Env for Client
  */
object ClientApp {

  val HOST = "https://jsonplaceholder.typicode.com"

  def getUser(
               key: Int
             ): ZIO[Client, Throwable, Response] = {
    val url = s"$HOST/users/$key"
    for {
      res <- Client.request(url)
      _ <- ZIO.logInfo(s"Called $url for cache")
      string <- res.body.asString
      response = Response.json(string).setStatus(res.status)
    } yield response
  }

  val waifuUrl = "https://api.waifu.pics/many/sfw/waifu"

  def getGirls: ZIO[Client, Throwable, Option[AnimeResponse]] = for {
    resp <- Client.request(url = waifuUrl, method = Method.POST,
      headers = Headers("Content-type", "application/x-www-form-urlencoded"),
      content = Body.fromString(AnimeRequest("").toJson))
    result <- resp.body.asString
  } yield result.fromJson[AnimeResponse].toOption

  // Quick and dirty, less than ideal
  def downloadGirl(girlUrl: String) = for {
    _ <- ZIO.succeed(println(girlUrl))
    resp <- Client.request(girlUrl)
    data <- resp.body.asArray
    _ <- ZIO.fromTry {
      Try {
        val filename = girlUrl.split("/").last
        println(s"Download $filename")
        val target = new BufferedOutputStream(new FileOutputStream(s"src/main/resources/waifu/${filename}"))
        try data.foreach(target.write(_)) finally target.close()
      }
    }
  } yield ()

  def apply(): Http[
    Client with Cache[Int, Throwable, Response],
    Throwable,
    Request,
    Response
  ] =
    Http.collectZIO[Request] {
      // directly passing on response
      case Method.GET -> !! / "client" / "users" =>
        val url = s"$HOST/users"
        for {
          res <- Client.request(url)
          _   <- ZIO.logInfo(s"Called $url")
        } yield res
      // getting json string and creating response from that
      case Method.GET -> !! / "client" / "users" / int(id) =>
        ZIO.service[Cache[Int, Throwable, Response]].flatMap(_.get(id))
      case Method.GET -> !! / "client" / "dopost" =>
        val url = s"$HOST/posts/"
        val json = for {
          res <- Client.request(
            url,
            method = Method.POST,
            headers =
              Headers("Content-type", "application/json; charset=UTF-8"),
            content = Body.fromString("""{
               |"title": "Hello",
               |"body": "World",
               |"userId": "1"
               |}
              |""".stripMargin)
          )
          _      <- ZIO.logInfo(s"Called $url")
          string <- res.body.asString
          post = string.fromJson[Post].toOption
          json = post.toJsonPretty
        } yield json
        json.map(Response.json(_))
      case Method.GET -> !! / "client" / "posts" =>
        val url = s"$HOST/posts/"
        val json = for {
          res    <- Client.request(url)
          _      <- ZIO.logInfo(s"Called $url")
          string <- res.body.asString
          posts = string.fromJson[List[Post]].toOption
          json  = posts.toJsonPretty
        } yield json
        json.map(Response.json(_))
      // we handle 404s
      case Method.GET -> !! / "client" / "posts" / int(id) =>
        val url = s"$HOST/posts/$id"
        for {
          _ <- ZIO.logInfo(s"about to call client for $url")
          res    <- Client.request(url)
          _      <- ZIO.logInfo(s"Called $url")
          string <- res.body.asString
        } yield
          if (res.status.isError) Response.text(string).setStatus(res.status)
          else Response.json(string.fromJson[Post].toOption.toJsonPretty)
      case Method.GET -> !! / "client" / "posts" / "userId" / int(userId) =>
        val url = s"$HOST/posts?userId=$userId"
        val json = for {
          res    <- Client.request(url)
          _      <- ZIO.logInfo(s"Called $url")
          string <- res.body.asString
          posts = string.fromJson[List[Post]].toOption
          json  = posts.toJsonPretty
        } yield json
        json.map(Response.json(_))
      case Method.GET -> !! / "client" / "girls"=>
        Files.createDirectories(Paths.get(s"src/main/resources/waifu/"))
        val json = for {
          files <- getGirls.map(_.map(_.files).getOrElse(Array.empty))
          _ <- ZIO.logInfo(files.mkString(","))
          _ <- ZIO.foreachPar(files)(downloadGirl)
        } yield s"Girls are downloaded: ${files.length}"
        json.logError *> json.map(Response.json(_))
    }

}
