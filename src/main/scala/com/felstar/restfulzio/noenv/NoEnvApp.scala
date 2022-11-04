package com.felstar.restfulzio.noenv

import com.felstar.restfulzio.client.Post
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.Console.printLine
import zio.Duration._
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
import zio.cache.{Cache, Lookup}
import zio.{URIO, ZIO, durationInt}
/**
 * An http app that:
 *   - Accepts a `Request` and returns a `Response`
 *   - Does not fail
 *   - Does not use Env
 */

case class SimplePost(string: String)

object SimplePost {
  implicit val encoder: JsonEncoder[SimplePost] =
    DeriveJsonEncoder.gen[SimplePost]
  implicit val decoder: JsonDecoder[SimplePost] =
    DeriveJsonDecoder.gen[SimplePost]

  implicit class SimplePostRequest( req: Request) {
    val simplePostOptionZIO: ZIO[Any, Nothing, Option[SimplePost]] = req.body.asString.orDie.map(_.fromJson[SimplePost].toOption)
  }

}


object NoEnvApp {

  import SimplePost._

  def apply(): Http[Any, Nothing, Request, Response] = {


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
          simplePostOption <- req.simplePostOptionZIO
          stringOut = simplePostOption.map(_.toString).getOrElse("")
        } yield Response.text(s"post is $stringOut")
    }
  }



}
