package com.felstar.restfulzio.staticserver


import zio._
import zio.http._
import zio.http.html._
import zio.Console.printLine
import zio.http.model.Method

import java.io.File

object StaticApp {

  def encodeForBrowser(path: Path): String = if (path.isEmpty) "/" else s"/$path"

  def pathToHttp(path: Path)={
    val myfile = new File("static", path.encode)
    for {
      http <-
        if (myfile.isDirectory) {
          val files = myfile.listFiles.toList.sortBy(_.getName)

          Http.template(s"File Explorer ~${encodeForBrowser(path)}") {
            ul(
              li(a(href := encodeForBrowser(path.dropLast(1)), "..")),
              files.map { file =>
                li(
                  a(
                    href := s"${encodeForBrowser(path / file.getName)}",
                    file.getName
                  )
                )
              }
            )
          }
        }
        else if (myfile.isFile) Http.fromFile(myfile)
        else Http.empty
    } yield http
  }

  def apply(): Http[Any, Throwable, Request, Response] =
    Http.collectHttp[Request] {

      case Method.GET -> "" /: path =>
        Http.fromZIO(printLine(s"StaticApp called on $path")) *> pathToHttp(path)
    }

}
