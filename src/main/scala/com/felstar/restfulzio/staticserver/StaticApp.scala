package com.felstar.restfulzio.staticserver


import zio._
import zio.http._
import zio.http.template._
import zio.Console.printLine

import java.io.File

object StaticApp {

  private val staticRoot = new File("static").getCanonicalFile

  def encodeForBrowser(segments: Iterable[String]): String = segments.mkString("/", "/", "")

  def pathToHandler(path: Path): Handler[Any, Throwable, Request, Response] = {
    val segments = path.segments
    // Canonicalise, so that ".." segments cannot escape the static dir
    val myfile = new File(staticRoot, segments.mkString("/")).getCanonicalFile
    val insideStaticRoot = myfile.toPath.startsWith(staticRoot.toPath)

    if (!insideStaticRoot) Handler.notFound
    else if (myfile.isDirectory) {
      val files = myfile.listFiles.toList.sortBy(_.getName)

      Handler.template(s"File Explorer ~${encodeForBrowser(segments)}") {
        ul(
          li(a(hrefAttr := encodeForBrowser(segments.dropRight(1)), "..")),
          files.map { file =>
            li(
              a(
                hrefAttr := encodeForBrowser(segments :+ file.getName),
                file.getName
              )
            )
          }
        )
      }
    }
    else if (myfile.isFile) Handler.fromFile(myfile)
    else Handler.notFound
  }

  // Matches every GET that no other, more specific, route does
  def apply(): Routes[Any, Throwable] = Routes(
    Method.GET / trailing -> Handler.fromFunctionHandler[(Path, Request)] { case (path: Path, _: Request) =>
      Handler
        .fromZIO(printLine(s"StaticApp called on $path"))
        .flatMap(_ => pathToHandler(path))
        .contramap[(Path, Request)](_._2)
    }
  )

}
