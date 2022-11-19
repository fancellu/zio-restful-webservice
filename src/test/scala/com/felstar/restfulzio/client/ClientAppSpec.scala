package com.felstar.restfulzio.client

import zio.http._
import zio.{Duration, Scope, URIO, ZLayer}
import zio.test._
import zio.cache.{Cache, Lookup}

import java.util.concurrent.TimeUnit

object ClientAppSpec extends ZIOSpecDefault {

  val userCache: URIO[Client, Cache[
    Int,
    Throwable,
    Response
  ]] = Cache.make(
    capacity = 100,
    timeToLive = Duration.apply(15, TimeUnit.SECONDS),
    lookup = Lookup(ClientApp.getUser)
  )

  private val app = ClientApp()

  def spec = suite("ClientSpec")(
    test("Using the real client (which a unit test shouldn't do, obviously)") {
      val path = !! / "client" / "users" / "1"

      val req = Request.get(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody.contains("Leanne Graham"))
    }.provide(
      Client.default,
      Scope.default,
      ZLayer.fromZIO(userCache)
    )
  )
}
