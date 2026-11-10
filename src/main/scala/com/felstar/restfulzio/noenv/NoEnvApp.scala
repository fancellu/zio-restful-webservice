package com.felstar.restfulzio.noenv

import zio._
import zio.http._
import zio.json._

final case class SimplePost(string: String)

object SimplePost {
  implicit class SimplePostRequest( req: Request) {
    val simplePostOptionZIO: ZIO[Any, Nothing, Option[SimplePost]] = req.body.asString.orDie.map(_.fromJson[SimplePost].toOption)
  }

  implicit val codec: JsonCodec[SimplePost] = DeriveJsonCodec.gen[SimplePost]
}

/**
 * Routes that:
 *   - Accept a `Request` and return a `Response`
 *   - Do not fail
 *   - Do not use Env
 */
object NoEnvApp {

  // Turns a plain function into a handler, decoding the request body from json, and encoding the result as json.
  // A body that is not valid json for `In` is a 400
  def jsonHandler[In: JsonDecoder, Out: JsonEncoder](f: In => Out): Handler[Any, Nothing, Request, Response] =
    handler { (req: Request) =>
      req.body.asString.orDie.map(_.fromJson[In] match {
        case Right(in) => Response.json(f(in).toJson)
        case Left(_)   => Response.badRequest("Invalid JSON!!")
      })
    }

  import SimplePost._

  def apply(): Routes[Any, Nothing] = Routes(
    // GET /noenv
    Method.GET / "noenv" -> Handler.text("Hello from noenv"),
    Method.GET / "headers" -> handler { (req: Request) =>
      Response.text(s"""headers are ${req.headers.toList.map(h => s"(${h.headerName},${h.renderedValue})").mkString("\n")}""")
    },
    Method.POST / "simplepost" -> handler { (req: Request) =>
      for {
        stringIn <- req.body.asString.orDie
        simplePostOption = stringIn.fromJson[SimplePost].toOption
        stringOut = simplePostOption.map(_.toString).getOrElse("")
      } yield Response.text(s"post is $stringOut")
    },
    Method.POST / "simplepost2" -> handler { (req: Request) => // using implicit class to add to Request
      for {
        simplePostOption <- req.simplePostOptionZIO
        stringOut = simplePostOption.map(_.toString).getOrElse("")
      } yield Response.text(s"post is $stringOut")
    },
    // No Request or Response in sight, just SimplePost => String
    Method.POST / "simplepostcodec" -> jsonHandler[SimplePost, String] {
      case SimplePost("Dino") => s"hello Dino!!!!"
      case SimplePost(string) => s"string is $string"
    }
  )

}
