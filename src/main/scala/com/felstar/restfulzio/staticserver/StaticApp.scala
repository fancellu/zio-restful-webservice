package com.felstar.restfulzio.staticserver


import zio._
import zio.http._
import zio.http.html._
import zio.Console.printLine
import zio.http.model.Method

import java.io.File

object StaticApp {

  def encodeForBrowser(path: Path): String = if (path.isEmpty) "/" else s"/$path"

  private val staticRoot = new File("static").getCanonicalFile

  def pathToHttp(path: Path)={
    // Canonicalise, so that ".." segments cannot escape the static dir
    val myfile = new File(staticRoot, path.encode).getCanonicalFile
    val insideStaticRoot = myfile.toPath.startsWith(staticRoot.toPath)
    for {
      http <-
        if (!insideStaticRoot) Http.empty
        else if (myfile.isDirectory) {
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
