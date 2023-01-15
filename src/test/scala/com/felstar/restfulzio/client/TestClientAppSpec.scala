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
              status = Status.NotFound, // Doesn't work with Ok, no idea why! Returns "null" on ok
              body = Body.fromCharSequence("facere repellat provident"),
              headers = Headers(HeaderNames.contentType, HeaderValues.textPlain)
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
