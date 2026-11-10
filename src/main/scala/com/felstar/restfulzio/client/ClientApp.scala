package com.felstar.restfulzio.client

import zio.Console.printLine
import zio.Duration._
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
import zio.cache.{Cache, Lookup}
import zio.{URIO, ZIO, durationInt}
import zio.http._
import zio.http.model.{Header, Headers, Method}

import java.io.{BufferedOutputStream, FileOutputStream}

case class Post(userId: Int, id: Int, title: String, body: String)

object Post {
  implicit val encoder: JsonEncoder[Post] =
    DeriveJsonEncoder.gen[Post]
  implicit val decoder: JsonDecoder[Post] =
    DeriveJsonDecoder.gen[Post]
}

case class DogResponse(message: Array[String])

object DogResponse {
  implicit val decoder: JsonDecoder[DogResponse] = DeriveJsonDecoder.gen[DogResponse]
  implicit val encoder: JsonEncoder[DogResponse] = DeriveJsonEncoder.gen[DogResponse]
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

  // Random dog pictures, free and keyless: https://dog.ceo/dog-api/
  val dogsUrl = "https://dog.ceo/api/breeds/image/random/5"

  def getDogs: ZIO[Client, Throwable, Option[DogResponse]] = for {
    resp <- Client.request(dogsUrl)
    result <- resp.body.asString
  } yield result.fromJson[DogResponse].toOption

  // Quick and dirty, less than ideal
  def downloadDog(dogUrl: String) = for {
    _ <- ZIO.succeed(println(dogUrl))
    resp <- Client.request(dogUrl)
    data <- resp.body.asArray
    _ <- ZIO.attemptBlocking {
      // getName strips any directory part, as the url is from a 3rd party
      val filename = new java.io.File(dogUrl.split("/").last).getName
      println(s"Download $filename")
      val dir = new java.io.File("src/main/resources/dogs")
      dir.mkdirs()
      val target = new BufferedOutputStream(new FileOutputStream(new java.io.File(dir, filename)))
      try target.write(data.toArray) finally target.close()
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
      case Method.GET -> !! / "client" / "dogs" =>
        val json = for {
          files <- getDogs.map(_.map(_.message).getOrElse(Array.empty))
          _ <- ZIO.logInfo(files.mkString(","))
          _ <- ZIO.foreach(files)(downloadDog)
        } yield s"Dogs are downloaded: ${files.length}"
        json.map(Response.json(_))
    }

}
