package com.felstar.restfulzio.client

import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
import zio.cache.Cache
import zio.ZIO
import zio.http._

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

/** Routes that:
  *   - Accept a `Request` and return a `Response`
  *   - Could fail
  *   - Use a `Client`, and a `Cache` of users
  */
object ClientApp {

  val HOST = "https://jsonplaceholder.typicode.com"

  // batched reads the whole body into memory, and looks after the connection for us
  private def get(url: String): ZIO[Client, Throwable, Response] =
    Client.batched(Request.get(url))

  def getUser(
               key: Int
             ): ZIO[Client, Throwable, Response] = {
    val url = s"$HOST/users/$key"
    for {
      res <- get(url)
      _ <- ZIO.logInfo(s"Called $url for cache")
      string <- res.body.asString
      response = Response.json(string).status(res.status)
    } yield response
  }

  // Random dog pictures, free and keyless: https://dog.ceo/dog-api/
  val dogsUrl = "https://dog.ceo/api/breeds/image/random/5"

  def getDogs: ZIO[Client, Throwable, Option[DogResponse]] = for {
    resp <- get(dogsUrl)
    result <- resp.body.asString
  } yield result.fromJson[DogResponse].toOption

  // Quick and dirty, less than ideal
  def downloadDog(dogUrl: String) = for {
    _ <- ZIO.succeed(println(dogUrl))
    resp <- get(dogUrl)
    data <- resp.body.asArray
    _ <- ZIO.attemptBlocking {
      // getName strips any directory part, as the url is from a 3rd party
      val filename = new java.io.File(dogUrl.split("/").last).getName
      println(s"Download $filename")
      val dir = new java.io.File("src/main/resources/dogs")
      dir.mkdirs()
      val target = new BufferedOutputStream(new FileOutputStream(new java.io.File(dir, filename)))
      try target.write(data) finally target.close()
    }
  } yield ()

  def apply(): Routes[
    Client with Cache[Int, Throwable, Response],
    Throwable
  ] = Routes(
    // getting json string and creating response from that
    Method.GET / "client" / "users" -> handler {
      val url = s"$HOST/users"
      for {
        res    <- get(url)
        _      <- ZIO.logInfo(s"Called $url")
        string <- res.body.asString
      } yield Response.json(string).status(res.status)
    },
    // from the cache
    Method.GET / "client" / "users" / int("id") -> handler { (id: Int, _: Request) =>
      ZIO.serviceWithZIO[Cache[Int, Throwable, Response]](_.get(id))
    },
    Method.GET / "client" / "dopost" -> handler {
      val url = s"$HOST/posts/"
      for {
        res <- Client.batched(
          Request
            .post(
              url,
              Body.fromString("""{
                 |"title": "Hello",
                 |"body": "World",
                 |"userId": "1"
                 |}
                |""".stripMargin)
            )
            .addHeader(Header.ContentType(MediaType.application.json))
        )
        _      <- ZIO.logInfo(s"Called $url")
        string <- res.body.asString
        post = string.fromJson[Post].toOption
      } yield Response.json(post.toJsonPretty)
    },
    Method.GET / "client" / "posts" -> handler {
      val url = s"$HOST/posts/"
      for {
        res    <- get(url)
        _      <- ZIO.logInfo(s"Called $url")
        string <- res.body.asString
        posts = string.fromJson[List[Post]].toOption
      } yield Response.json(posts.toJsonPretty)
    },
    // we handle 404s
    Method.GET / "client" / "posts" / int("id") -> handler { (id: Int, _: Request) =>
      val url = s"$HOST/posts/$id"
      for {
        _      <- ZIO.logInfo(s"about to call client for $url")
        res    <- get(url)
        _      <- ZIO.logInfo(s"Called $url")
        string <- res.body.asString
      } yield
        if (res.status.isError) Response.text(string).status(res.status)
        else Response.json(string.fromJson[Post].toOption.toJsonPretty)
    },
    Method.GET / "client" / "posts" / "userId" / int("userId") -> handler { (userId: Int, _: Request) =>
      val url = s"$HOST/posts?userId=$userId"
      for {
        res    <- get(url)
        _      <- ZIO.logInfo(s"Called $url")
        string <- res.body.asString
        posts = string.fromJson[List[Post]].toOption
      } yield Response.json(posts.toJsonPretty)
    },
    Method.GET / "client" / "dogs" -> handler {
      for {
        files <- getDogs.map(_.map(_.message).getOrElse(Array.empty[String]))
        _     <- ZIO.logInfo(files.mkString(","))
        _     <- ZIO.foreachDiscard(files)(downloadDog)
      } yield Response.json(s"Dogs are downloaded: ${files.length}")
    }
  )

}
