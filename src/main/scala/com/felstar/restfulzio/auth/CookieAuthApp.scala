package com.felstar.restfulzio.auth

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio._

import zio.http._
import zio.http.model.{Cookie, Method, Status}

import java.time.Clock

object CookieAuthApp {

  val SECRET_KEY = "secretKey!!"

  implicit val clock: Clock = Clock.systemUTC

  def jwtEncode(username: String): String = {
    val json = s"""{"user": "${username}"}"""
    val claim = JwtClaim {
      json
    }.issuedNow.expiresIn(300)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)
  }

  def jwtDecode(token: String): Option[JwtClaim] = {
    Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS512)).toOption
  }

  def greet: UHttpApp = Http.collectZIO[Request] { case req @ Method.GET -> !! / "auth" / name / "greet" =>
    val csOption = req.cookieValue("jwt")
    val valid = (for {
      cs <- csOption
      jwtClaim <- jwtDecode(cs.toString)
    } yield (jwtClaim.content == s"""{"user": "${name}"}""") && jwtClaim.isValid).getOrElse(false)
    for {
      _ <- ZIO.logInfo(s"Attempting to greet logged in user, cookie csOption=$csOption, $valid")
    } yield if (valid) Response.text(s"You are logged in! ${name}") else
      Response.text(s"You are not logged in, sorry")
  }

  // Login is successful only if the password is the reverse of the username
  def login: UHttpApp = Http.collectZIO[Request] { case Method.GET -> !! / "login" / username / password =>
    for {
     _ <- ZIO.logInfo("Attempting to log in")
    } yield if (password.reverse == username)
      Response.text("Logged in").addCookie(Cookie("jwt", jwtEncode(username)).withPath(!!))
    else Response.text("Invalid username or password.").setStatus(Status.Unauthorized)
  }

  def apply() =  login ++ greet

}
