package com.felstar.restfulzio.actors


import zio.actors.Actor.Stateful
import zio.actors._
import zio.{ZIO, ZLayer, durationInt}
import zio.http._
import zio.http.model.Method

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

  // One actor system and one actor for the whole app, shut down when the app stops
  val layer: ZLayer[Any, Throwable, ActorRef[Command]] = ZLayer.scoped {
    for {
      system <- ZIO.acquireRelease(ActorSystem("mySystem"))(_.shutdown.ignore)
      actor  <- system.make("actor1", Supervisor.none, (), stateful)
    } yield actor
  }

  val doDouble: ZIO[ActorRef[Command] with Int, Throwable, Int] = for {
    actor   <- ZIO.service[ActorRef[Command]]
    i       <- ZIO.service[Int]
    _       <- ZIO.logInfo(s"Calling actor with $i")
    doubled <- actor ? DoubleCommand(i)
    _       <- ZIO.logInfo(s"Returned from actor wait")
  } yield doubled

  val doBang: ZIO[ActorRef[Command] with String, Throwable, String] = for {
    actor  <- ZIO.service[ActorRef[Command]]
    string <- ZIO.service[String]
    _      <- ZIO.logInfo(s"Calling actor with $string")
    banged <- actor ? BangCommand(string)
    _      <- ZIO.logInfo(s"Returned from actor wait")
  } yield banged

  def apply(): Http[ActorRef[Command], Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "actors" =>
        for {
          out <- doDouble.provideSome[ActorRef[Command]](ZLayer.succeed(42)).orDie
        } yield Response.text(s"Actor returned $out")
      case Method.GET -> !! / "actors" / int(i) =>
        for {
          out <- doDouble.provideSome[ActorRef[Command]](ZLayer.succeed(i)).orDie
        } yield Response.text(s"Actor returned $out")

      case Method.GET -> !! / "actors" / "nodie" =>
        for {
          out <- doBang
            .provideSome[ActorRef[Command]](ZLayer.succeed("nodie"))
            .orElseSucceed(
              s"fallback after error"
            )
        } yield Response.text(s"Actor returned $out")
      case Method.GET -> !! / "actors" / "catchAll" =>
        for {
          out <- doBang.provideSome[ActorRef[Command]](ZLayer.succeed("catchAll")).catchAll {
            case th: Throwable => ZIO.succeed(s"fallback because $th")
          }
        } yield Response.text(s"Actor returned $out")
      case Method.GET -> !! / "actors" / string =>
        for {
          out <- doBang.provideSome[ActorRef[Command]](
            ZLayer.succeed(string)
          ) // you will get exception logged by errorMiddleware, not displayed to screen
        } yield Response.text(s"Will never return  $out")

    }

}
