package com.felstar.restfulzio.noenv

import com.felstar.restfulzio.client.Post
import com.felstar.restfulzio.noenv
import zhttp.http.{Http, _}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.Console.printLine
import zio.Duration._
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
import zio.cache.{Cache, Lookup}
import zio.{URIO, ZIO, durationInt}
import zio.json._
import zhttp.http.middleware._
import zhttp.http.middleware.HttpMiddleware

import scala.language.postfixOps


/**
 * An http app that:
 *   - Accepts a `Request` and returns a `Response`
 *   - Does not fail
 *   - Does not use Env
 */

final case class SimplePost(string: String)

object SimplePost {
  implicit class SimplePostRequest( req: Request) {
    val simplePostOptionZIO: ZIO[Any, Nothing, Option[SimplePost]] = req.body.asString.orDie.map(_.fromJson[SimplePost].toOption)
  }

  implicit val codec: JsonCodec[SimplePost] = DeriveJsonCodec.gen[SimplePost]
}

object NoEnvApp {

  def codecMiddleware[In: JsonDecoder, Out: JsonEncoder]: Middleware[Any, Nothing, In, Out, Request, Response] =
    Middleware.codecZIO[Request, Out](
      request =>
        for {
          body <- request.body.asString
          either = JsonDecoder[In].decodeJson(body)
          in <- ZIO.fromEither(either)
        } yield in,
      out => {
        for {
          charSeq <- ZIO.succeed(JsonEncoder[Out].encodeJson(out, None))
          _ <- ZIO.log(charSeq.toString)
        } yield Response.json(charSeq.toString)
      }
    ) <> Middleware.succeed(Response.fromHttpError(HttpError.BadRequest("Invalid JSON!!")))


  import SimplePost._

  def apply(): Http[Any, Nothing, Request, Response] = {

    val simplePostService: Http[Any, Nothing, SimplePost, String] = Http.collect {
      case SimplePost("Dino") => s"hello Dino!!!!"
      case SimplePost(string) => s"string is $string"
    }

    val simplePostCodec: Http[Any, Nothing, Request, Response] = simplePostService @@ codecMiddleware[SimplePost, String]

    Http.collect[Request] {
      // GET /noenv
      case Method.GET -> !! / "noenv" => Response.text(s"Hello from noenv")
      case req@Method.GET -> !! / "headers" => Response.text(s"""headers are ${req.headers.toList.mkString("\n")}""")
    } ++
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "simplepost" =>
        for {
          stringIn <- req.body.asString.orDie
          simplePostOption = stringIn.fromJson[SimplePost].toOption
          stringOut = simplePostOption.map(_.toString).getOrElse("")
        } yield Response.text(s"post is $stringOut")
      case req@Method.POST -> !! / "simplepost2" => // using implicit class to add to Request
        for {
          simplePostOption<- req.simplePostOptionZIO
          stringOut = simplePostOption.map(_.toString).getOrElse("")
        } yield Response.text(s"post is $stringOut")
    } ++
      Http.collectZIO[Request]  {
      case req@Method.POST -> !! / "simplepostcodec" => simplePostCodec(req).orElse(ZIO.never)
    }
  }

}
