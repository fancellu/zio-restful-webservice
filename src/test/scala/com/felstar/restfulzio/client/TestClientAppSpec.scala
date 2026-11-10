package com.felstar.restfulzio.client

import com.felstar.restfulzio.MainApp
import zio.cache.{Cache, Lookup}
import zio.http._
import zio.http.model.{HeaderNames, HeaderValues, Headers, Status}
import zio.test._
import zio.{Duration, Scope, URIO, ZIO, ZLayer}

import java.util.concurrent.TimeUnit

object TestClientAppSpec extends ZIOSpecDefault {

  val userCache: URIO[Client, Cache[
    Int,
    Throwable,
    Response
  ]] = Cache.make(
    capacity = 100,
    timeToLive = Duration.apply(15, TimeUnit.SECONDS),
    lookup = Lookup(ClientApp.getUser)
  )

  def spec = suite("TestClientSpec")(
    test("Using TestClient") {
      val path = !! / "client" / "posts" / "1"
      val app = ClientApp()

      for {
        _ <- TestClient.addHandler { case req: Request =>
          ZIO.logInfo(s"Test client being called on $req") *> ZIO.succeed(
            Response(
              // On Ok the body has to be a valid Post, as ClientApp parses it (anything else becomes "null")
              status = Status.Ok,
              body = Body.fromCharSequence("""{"userId":1,"id":1,"title":"facere repellat provident","body":"some body"}"""),
              headers = Headers(HeaderNames.contentType, HeaderValues.applicationJson)
            )
          )
        }

        expectedBody <-  app(Request.get(url = URL(path))).flatMap(_.body.asString)
       } yield assertTrue(expectedBody.contains("repellat"))
    }
  ).provide(
    TestClient.layer,
    ZLayer.fromZIO(userCache)
  )
}
