package com.felstar.restfulzio.spark

import zio._
import zio.http._
import zio.http.model.Method
import org.apache.spark.sql.Row
import zio.spark.experimental
import zio.spark.experimental.Pipeline
import zio.spark.parameter._
import zio.spark.rdd.RDD
import zio.spark.sql._
import zio.spark.sql.implicits._

/** An http app that:
  *   - Accepts a `Request` and returns a `Response`
  *   - Does not fail
  *   - Uses a String for the env, for the webapp root
  */
object SparkApp {

  import zio.spark.sql.TryAnalysis.syntax.throwAnalysisException

  final case class Person(name: String, age: Int)

  val transform: DataFrame => Dataset[Person] = _.as[Person]

  val headOption: Dataset[Person] => Task[Option[Person]] = _.headOption

  val csv: SIO[DataFrame] = SparkSession.read
    .schema[Person]
    .withHeader
    .csv("src/main/resources/data.csv")

  val buildsbt: SIO[Dataset[String]] = SparkSession.read.textFile("build.sbt")

  def wordCount(inputDs: Dataset[String]): RDD[(String, Int)] =
    inputDs
      .flatMap(line => line.trim.split(" "))
      .flatMap(word => word.split('.'))
      .map(_.replaceAll("[^a-zA-Z]", ""))
      .filter(_.length > 1)
      .map(word => (word, 1))
      .rdd
      .reduceByKey(_ + _)

  val wordCountZIO: ZIO[SparkSession, Throwable, Seq[(String, Int)]] =
    for {
      words <- buildsbt.map(wordCount).flatMap(_.collect)
      mostUsedWords = words.sortBy(_._2).reverse
    } yield mostUsedWords

  val jobZIO: ZIO[SparkSession, Throwable, String] =
    for {
      somePeople <- experimental.Pipeline(csv, transform, headOption).run
      st = somePeople
        .map(p => s"The first person's name is ${p.name}.")
        .getOrElse("Nobody there")
    } yield st

  val allOutput: Dataset[Person] => Task[Iterator[Person]] = _.toLocalIterator

  val allZIO: ZIO[SparkSession, Throwable, String] =
    for {
      people <- experimental.Pipeline(csv, transform, allOutput).run
      st = people.mkString(",")
    } yield st

  def filterByName(name: String): DataFrame => Dataset[Person] =
    _.as[Person].filter(_.name == name)

  val byNameZIO: ZIO[SparkSession with String, Throwable, String] =
    for {
      name   <- ZIO.service[String]
      people <- experimental.Pipeline(csv, filterByName(name), allOutput).run
      st = people.mkString(",")
    } yield st

  def apply(): Http[SparkSession, Throwable, Request, Response] =
    Http.fromZIO(ZIO.service[SparkSession]).flatMap { ss =>
      val ssLayer = ZLayer.succeed(ss)
      val SPARKROOT = !! / "spark"
      Http.collectZIO[Request] {
        case Method.GET -> SPARKROOT =>
          for {
            st <- allZIO.provide(ssLayer)
            _  <- ZIO.logInfo(st)
          } yield Response.text(st)
        case Method.GET -> SPARKROOT / "wordcount" =>
          for {
            mostUsedWords <- wordCountZIO.provide(ssLayer)
            _             <- ZIO.logInfo(mostUsedWords.toString)
          } yield Response.text(mostUsedWords.mkString("\n"))
        case Method.GET -> SPARKROOT / "person" / name =>
          for {
            st <- byNameZIO.provide(ssLayer, ZLayer.succeed(name))
            _  <- ZIO.logInfo(st)
          } yield Response.text(st)
        case Method.GET -> SPARKROOT / "job" =>
          for {
            st <- jobZIO.provide(ssLayer)
            _  <- ZIO.logInfo(st)
          } yield Response.text(st)
      }
    }
}
