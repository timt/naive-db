import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax.EncoderOps
import org.scalatest.{FreeSpec, Matchers}

import scala.io.Source
import scala.reflect.io.File

class JsonDbSpec extends FreeSpec with Matchers {

  case class Post(id: Int, title: String)

  object Post {
    implicit val decoder: Decoder[Post] = deriveDecoder[Post]
    implicit val encoder: Encoder[Post] = deriveEncoder[Post]
  }


  "Can set an item value" in {
    Db
      .initialize
      .item[Post]("post")
      .set(Post(1, "some new thing"))

    Source.fromFile("db.json").mkString should be
    """
        |{
        |  "post" : {
        |    "id" : 1,
        |    "title" : "some new thing"
        |  }
        |}
      """.stripMargin

    Db
      .initialize
      .set[Post]("post", Post(1, "another new thing"))

    Source.fromFile("db.json").mkString should be
    """
        |{
        |  "post" : {
        |    "id" : 1,
        |    "title" : "another new thing"
        |  }
        |}
      """.stripMargin
  }

  "Can get a value" in {
    val db = Db
      .initialize
      .set[Post]("post", Post(1, "another new thing"))

    val post: Post = db.item[Post]("post").value

  }

  "Can add a value to to list" in {

  }

  "Can find a value in a list" in {

  }

  "Can modify a value in a list" in {

  }

  "Can remove a value" in {

  }

  "Can remove values from a list"

  //Small library that can put, save/update query json file (CATS/Argonaut)
 /*
 some sort of Monad here?

 for {
  posts <- get[List[Post]]("currentPosts")
  post  <- find[Post](_.id = 1)
  _     <- set[List[Post]]("historicalPosts", List(Post(...), Post(...)))
  _     <- push[Post](Post(...))
  _     <- delete("someOtherListOfPosts")
  -     <- remove[
 } yield thing
 verbs
 add/set
 replace

 https://github.com/typicode/lowdb
 // Set some defaults if your JSON file is empty
db.defaults({ posts: [], user: {} })
  .write()

// Add a post
db.get('posts')
  .push({ id: 1, title: 'lowdb is awesome'})
  .write()

// Set a user
db.set('user.name', 'typicode')
  .write()

  db.get('posts')
  .find({ id: 1 })
  .value()
  */

}

class DbItem(name: String, db: Db) {
  def set[A: Encoder](item: A): Db = db.set[A](name, item)
  def value[A](implicit decode: Decoder[A]) : A = db.valueFor[A](name)
}

case class Db(values: Map[String, Json ] = Map()){
  //  def load: Db = {
//      Db(parse(Source.fromFile(dbFileName)("UTF-8").mkString)
//        .getOrElse(throw new RuntimeException("boom!"))
//        .asObject.map(_.toMap)
//        .getOrElse(Map())
//      )
//  }

  private def save: Db = {
    File("db.json").writeAll(values.asJson.spaces2)
    this
  }

  def item[A](name: String)(implicit decode: Decoder[A]): DbItem = new DbItem(name, this)

  def set[A: Encoder](item: String, value: A): Db = {
    copy(values = values + (item -> value.asJson))
    save
  }

  def valueFor[A: Decoder](name: String): A = {
    val map = values.get(name).map(_.as[A])
    ???
  }

}
//class Db() {
//  val file = File("db.json")
//
//  if (!file.exists || file.length == 0){
//    initialize
//  }
//
//  def load = ???
//  def save = ???
//
//  def initialize: Db = {
//    val asJson: Json = Map[String, String]().asJson
//    file.writeAll(asJson.spaces2)
//    this
//  }
//
//  def item[A](name: String)(implicit decode: Decoder[A]): DbItem[A] = new DbItem(name, this)
//
////  def commit: Db =  file.writeAll(value.asJson.spaces2)
//
//  def set[A](item: String, value: A)(implicit encode: Encoder[A]): Db = {
//    ???
//  }

//  def push[A](item: String, value: A)(implicit encode: Encoder[A]): Db = {
//    ???
//  }
//
//}

object Db {
  val file = File("db.json")

  def initialize = new Db()

//  def set[A](itemName: String, value: A)(implicit encode: Encoder[A]): Unit =
//    File("db.json").writeAll(value.asJson.spaces2)
}
