package com.felstar.restfulzio.staticserver

package example

import zhttp.html._
import zhttp.http._
import zio.Console.printLine

import java.io.File

object StaticApp {

  def encodeForBrowser(path: Path): String = if (path.isEmpty) "/" else s"/$path"

  def pathToHttp(path: Path)={
    val myfile = new File("static", path.encode)
    for {
//      _ <- Http.fromZIO(printLine(s"path=$path"))
//      _ <- Http.fromZIO(printLine(path.encode))
//      _ <- Http.fromZIO(printLine(s"isDir ${myfile.isDirectory}"))
//      _ <- Http.fromZIO(printLine(s"isFile ${myfile.isFile}"))
//      _ <- Http.fromZIO(printLine(s"exists ${myfile.exists()}"))
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
