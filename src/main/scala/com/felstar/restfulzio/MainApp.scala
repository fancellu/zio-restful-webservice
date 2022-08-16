package com.felstar.restfulzio

import com.felstar.restfulzio.counter.CounterApp
import com.felstar.restfulzio.delay.DelayApp
import com.felstar.restfulzio.download.DownloadApp
import com.felstar.restfulzio.hellotwirl.HelloTwirlApp
import com.felstar.restfulzio.helloworld.HelloWorldApp
import com.felstar.restfulzio.noenv.NoEnvApp
import com.felstar.restfulzio.videos.{InmemoryVideoRepo, PersistentVideoRepo, VideoApp}
import zhttp.http.Middleware
import zhttp.service.Server
import zio._

object MainApp extends ZIOAppDefault {

  val middlewares = Middleware.dropTrailingSlash

  def run =
    Server.start(

      port = 8080,
      http = (NoEnvApp() ++ HelloWorldApp() ++ DownloadApp() ++ CounterApp() ++ VideoApp() ++ HelloTwirlApp() ++ DelayApp()) @@ middlewares
    ).provide(
      // For `CounterApp`
      ZLayer.fromZIO(Ref.make(0)),
      // For `HelloWorldApp`
      ZLayer.succeed("hello"),
      // To use the H2 DB layer, provide the `PersistentVideoRepo.layer` layer instead
       InmemoryVideoRepo.layer
      // PersistentVideoRepo.layer
    ).exitCode
}
