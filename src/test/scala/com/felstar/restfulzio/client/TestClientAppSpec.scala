package com.felstar.restfulzio.client

import zio.cache.{Cache, Lookup}
import zio.http._
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

  private val app = ClientApp().sandbox

  def spec = suite("TestClientSpec")(
    test("Using TestClient") {
      for {
        // Whatever ClientApp asks the outside world for, it gets this back
        _ <- TestClient.setFallbackHandler { (req: Request) =>
          ZIO.logInfo(s"Test client being called on ${req.url.encode}") *> ZIO.succeed(
            Response(
              // On Ok the body has to be a valid Post, as ClientApp parses it (anything else becomes "null")
              status = Status.Ok,
              body = Body.fromString("""{"userId":1,"id":1,"title":"facere repellat provident","body":"some body"}"""),
              headers = Headers(Header.ContentType(MediaType.application.json))
            )
          )
        }

        expectedBody <- app.runZIO(Request.get("/client/posts/1")).flatMap(_.body.asString)
       } yield assertTrue(expectedBody.contains("repellat"))
    }
  ).provide(
    TestClient.layer,
    Scope.default,
    ZLayer.fromZIO(userCache)
  )
}
