package com.felstar.restfulzio.spark

import zio._
import zio.http._
import org.apache.spark.sql.Row
import zio.spark.experimental
import zio.spark.experimental.Pipeline
import zio.spark.parameter._
import zio.spark.rdd.RDD
import zio.spark.sql._
import zio.spark.sql.implicits._

/** Routes that:
  *   - Accept a `Request` and return a `Response`
  *   - Could fail
  *   - Use a `SparkSession`
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

  def apply(): Routes[SparkSession, Throwable] = Routes(
    Method.GET / "spark" -> handler {
      for {
        st <- allZIO
        _  <- ZIO.logInfo(st)
      } yield Response.text(st)
    },
    Method.GET / "spark" / "wordcount" -> handler {
      for {
        mostUsedWords <- wordCountZIO
        _             <- ZIO.logInfo(mostUsedWords.toString)
      } yield Response.text(mostUsedWords.mkString("\n"))
    },
    Method.GET / "spark" / "person" / string("name") -> handler { (name: String, _: Request) =>
      for {
        st <- byNameZIO.provideSome[SparkSession](ZLayer.succeed(name))
        _  <- ZIO.logInfo(st)
      } yield Response.text(st)
    },
    Method.GET / "spark" / "job" -> handler {
      for {
        st <- jobZIO
        _  <- ZIO.logInfo(st)
      } yield Response.text(st)
    }
  )
}
