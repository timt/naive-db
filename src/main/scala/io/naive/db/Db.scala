package io.naive.db

import java.nio.file.{Files, Paths}

import io.circe.parser.parse
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

import scala.io.Source
import scala.reflect.io.File

case class Db(values: Map[String, Json] = Map(), file: Option[String] = None) {

  def set[T](item: String, value: T)(implicit encoder: Encoder[T]): Db = {
    copy(values = values + (item -> value.asJson))
      .save
  }

  def get[T](name: String)(implicit decoder: Decoder[T]): Option[T] = values.get(name).flatMap(_.as[T].toOption)

  private def getList[T](listName: String)(implicit decoder: Decoder[T]): List[T] = get[List[T]](listName).getOrElse(List.empty[T])

  def addItem[T](listName: String, item: T)(implicit encoder: Encoder[T], decoder: Decoder[T]): Db = set[List[T]](listName, (item :: getList(listName)).reverse)

  def find[T](listName: String, predicate: T => Boolean)(implicit encoder: Encoder[T], decoder: Decoder[T]): Option[T] = get[List[T]](listName).flatMap(_.find(predicate))

  def removeItem[T](listName: String, predicate: T => Boolean)(implicit encoder: Encoder[T], decoder: Decoder[T]): Db = {
    set(listName, getList(listName).filterNot(predicate))
  }

  def remove(name: String): Db = {
    copy(values = values.filterKeys(_ != name))
      .save
  }

  private def save: Db = {
    file.foreach(File(_).writeAll(values.asJson.spaces2))
    this
  }

}

object Db {

  def initialize(file: String): Db = {
    if (Files.exists(Paths.get(file)) && (Files.size(Paths.get(file)) > 0)) {
      Db(parse(Source.fromFile(file)("UTF-8").mkString)
        .getOrElse(throw new RuntimeException(s"Oops! Can not load file [$file]"))
        .asObject.map(_.toMap)
        .getOrElse(Map()),
        Some(file))
    } else Db(file = Some(file)).save
  }

  def inMemory: Db = new Db()

}