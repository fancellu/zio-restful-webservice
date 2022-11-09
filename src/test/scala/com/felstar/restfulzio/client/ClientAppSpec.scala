package com.felstar.restfulzio.client

import com.felstar.restfulzio.MainApp.userCache
import zhttp.http._
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{Duration, URIO, ZLayer}
import zio.test._
import zio.cache.{Cache, Lookup}

import java.util.concurrent.TimeUnit

object ClientAppSpec extends ZIOSpecDefault {

  val userCache: URIO[EventLoopGroup with ChannelFactory, Cache[
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

      val req = Request(url = URL(path))

      for {
        expectedBody <- app(req).flatMap(_.body.asString)
      } yield assertTrue(expectedBody.contains("Leanne Graham"))
    }.provide(
      ChannelFactory.auto,
      EventLoopGroup.auto(),
      ZLayer.fromZIO(userCache)
    )
  )
}
