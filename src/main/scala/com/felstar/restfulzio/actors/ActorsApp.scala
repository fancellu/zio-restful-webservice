package com.felstar.restfulzio.actors

import zhttp.http.{Http, Method, Request, Response}
import zio.actors.Actor.Stateful
import zio.actors._
import zio.{UIO, ZIO, ZLayer, durationInt}
import zhttp.http._
import zio.config.PropertyType.ZioDurationType

object ActorsApp {

  sealed trait Command[+T]

  case class DoubleCommand(value: Int)  extends Command[Int]
  case class BangCommand(value: String) extends Command[String]

  val stateful: Stateful[Any, Unit, Command] =
    new Stateful[Any, Unit, Command] {
      override def receive[A](
          state: Unit,
          msg: Command[A],
          context: Context
      ): ZIO[Any, Throwable, (Unit, A)] =
        msg match {
          case DoubleCommand(value) =>
            ZIO.logInfo(s"Actor called with $value, sleeping") *> ZIO.sleep(
              3.second
            ) *> ZIO.succeed(((), value * 2))
          case BangCommand("bang2") =>
            ZIO.logInfo(s"Actor called with bang2, sleeping") *> ZIO.sleep(
              2.second
            ) *> ZIO.fail(new Exception("I went bang2"))
          case BangCommand(value) =>
            ZIO.logInfo(
              s"Actor called with BangCommand($value), sleeping"
            ) *> ZIO.sleep(1.second) *> ZIO.fail(new Exception("I went bang"))
        }
    }

  val doDouble: ZIO[Int, Throwable, Int] = for {
    system  <- ActorSystem("mySystem")
    actor   <- system.make("actor1", Supervisor.none, (), stateful)
    i       <- ZIO.service[Int]
    _       <- ZIO.logInfo(s"Calling actor with $i")
    doubled <- actor ? DoubleCommand(i)
    _       <- ZIO.logInfo(s"Returned from actor wait")
  } yield doubled

  val doBang: ZIO[String, Throwable, String] = for {
    system <- ActorSystem("mySystem")
    actor  <- system.make("actor1", Supervisor.none, (), stateful)
    string <- ZIO.service[String]
    _      <- ZIO.logInfo(s"Calling actor with $string")
    banged <- actor ? BangCommand(string)
    _      <- ZIO.logInfo(s"Returned from actor wait")
  } yield banged

  def apply(): Http[Any, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "actors" =>
        for {
          out <- doDouble.provide(ZLayer.succeed(42)).orDie
        } yield Response.text(s"Actor returned $out")
      case Method.GET -> !! / "actors" / int(i) =>
        for {
          out <- doDouble.provide(ZLayer.succeed(i)).orDie
        } yield Response.text(s"Actor returned $out")

      case Method.GET -> !! / "actors" / "nodie" =>
        for {
          out <- doBang
            .provide(ZLayer.succeed("nodie"))
            .orElseSucceed(
              s"fallback after error"
            )
        } yield Response.text(s"Actor returned $out")
      case Method.GET -> !! / "actors" / "catchAll" =>
        for {
          out <- doBang.provide(ZLayer.succeed("catchAll")).catchAll {
            case th: Throwable => ZIO.succeed(s"fallback because $th")
          }
        } yield Response.text(s"Actor returned $out")
      case Method.GET -> !! / "actors" / string =>
        for {
          out <- doBang.provide(
            ZLayer.succeed(string)
          ) // you will get exception logged by errorMiddleware, not displayed to screen
        } yield Response.text(s"Will never return  $out")

    }

}
