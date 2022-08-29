package com.felstar.restfulzio.client

import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.Console.printLine
import zio.ZIO
import zio.json.{
  DecoderOps,
  DeriveJsonDecoder,
  DeriveJsonEncoder,
  EncoderOps,
  JsonDecoder,
  JsonEncoder
}

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

  def apply()
      : Http[EventLoopGroup with ChannelFactory, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // directly passing on response
      case Method.GET -> !! / "client" / "users" =>
        val url = s"$HOST/users"
        for {
          res <- Client.request(url)
        } yield res
      // getting json string and creating response from that
      case Method.GET -> !! / "client" / "users" / int(id) =>
        val url = s"$HOST/users/$id"
        for {
          res    <- Client.request(url)
          string <- res.body.asString
          response = Response.json(string).setStatus(res.status)
        } yield response
      // response=>string=>Option[List[Post]]=>json string
      case Method.GET -> !! / "client" / "posts" =>
        val url = s"$HOST/posts/"
        val json = for {
          res    <- Client.request(url)
          string <- res.body.asString
          posts = string.fromJson[List[Post]].toOption
          json  = posts.toJsonPretty
        } yield json
        json.map(Response.json(_))
      // we handle 404s
      case Method.GET -> !! / "client" / "posts" / int(id) =>
        val url = s"$HOST/posts/$id"
        for {
          res    <- Client.request(url)
          string <- res.body.asString
        } yield
          if (res.status.isError) Response.text(string).setStatus(res.status)
          else Response.json(string.fromJson[Post].toOption.toJsonPretty)
      case Method.GET -> !! / "client" / "posts" / "userId" / int(userId) =>
        val url = s"$HOST/posts?userId=$userId"
        val json = for {
          res    <- Client.request(url)
          string <- res.body.asString
          posts = string.fromJson[List[Post]].toOption
          json  = posts.toJsonPretty
        } yield json
        json.map(Response.json(_))

    }

}
