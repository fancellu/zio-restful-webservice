package com.felstar.restfulzio.client

import zio.Console.printLine
import zio.Duration._
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
import zio.cache.{Cache, Lookup}
import zio.{URIO, ZIO, durationInt}
import zio.http._
import zio.http.model.{Header, Headers, Method}

case class Post(userId: Int, id: Int, title: String, body: String)

object Post {
  implicit val encoder: JsonEncoder[Post] =
    DeriveJsonEncoder.gen[Post]
  implicit val decoder: JsonDecoder[Post] =
    DeriveJsonDecoder.gen[Post]
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

    }

}
