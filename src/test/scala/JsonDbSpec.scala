import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax.EncoderOps
import org.scalatest.{FreeSpec, Matchers}

import scala.io.Source
import scala.reflect.io.File
import io.circe.parser.parse


class JsonDbSpec extends FreeSpec with Matchers {

  case class Post(id: Int, title: String)

  object Post {
    implicit val decoder: Decoder[Post] = deriveDecoder[Post]
    implicit val encoder: Encoder[Post] = deriveEncoder[Post]
  }

  val testFile: String = "target/db.json"

  val emptyJsonObject: Json = JsonObject.empty.asJson

  def json(jsonString: String): Json = parse(jsonString).getOrElse(throw new RuntimeException(s"boom! \n===\n$jsonString\n==="))
  def jsonFile(file: String = testFile): Json = json(Source.fromFile(file).mkString)

  "can create new db in named file" in {
    Db.initialize("/tmp/db.json")
    jsonFile("/tmp/db.json") shouldBe emptyJsonObject
  }

  "Can load db from named file" in {
//    val file = java.io.File.createTempFile(this.getClass.getSimpleName, ".db")
//    File(file.getAbsolutePath).writeAll(Map("post" -> Post(3, "some ting")).asJson.spaces2)
//    Db.initialize(file.getAbsolutePath)
//        .get[Post]("post") shouldBe Post(3, "some ting")
  }

  "load db from named file creates file if missing" in {

  }


  "Can set an item value" in {

    Db
      .initialize(testFile)
      .set("post", Post(1, "another new thing"))

    jsonFile() shouldBe json("""{
        |  "post" : {
        |    "id" : 1,
        |    "title" : "another new thing"
        |  }
        |}
      """.stripMargin)

  }

  "Can get a value" in {
    val db = Db
      .initialize(testFile)
      .set("post", Post(1, "another new thing"))

    val item: Option[Post] = db.get[Post]("post")

    item shouldBe Some(Post(1, "another new thing"))
  }

  "Can add a value to to list" in {
    val db = Db
      .initialize(testFile)
      .addItem[Post]("posts", Post(1, "some post"))

    jsonFile(testFile) shouldBe json(
      """
        |{
        |  "posts" : [ {
        |     "id": 1,
        |     "title": "some post"
        |  }]
        |}
      """.stripMargin)

    db
      .addItem[Post]("posts", Post(2, "another post"))

    jsonFile(testFile) shouldBe json(
      """
        |{
        |  "posts" : [
        |  {
        |     "id": 1,
        |     "title": "some post"
        |  },
        |  {
        |     "id": 2,
        |     "title": "another post"
        |  }
        |  ]
        |}
      """.stripMargin)


  }

  "Can find a value in a list" in {
    val post = Db
      .initialize(testFile)
      .addItem[Post]("posts", Post(1, "some post"))
      .addItem[Post]("posts", Post(2, "another post"))
      .find[Post]("posts", _.title.contains("another"))

    post shouldBe Some(Post(2, "another post"))

  }

  "Can remove a value" in {
    val db = Db
      .initialize(testFile)
      .set("post", Post(1, "another new thing"))
      .addItem[Post]("posts", Post(1, "some post"))

    jsonFile(testFile) shouldBe json(
      """{
        |  "post" : {
        |    "id" : 1,
        |    "title" : "another new thing"
        |  },
        |  "posts" : [
        |    {
        |      "id" : 1,
        |      "title" : "some post"
        |    }
        |  ]
        |}
      """.stripMargin)

    val updatedDb = db.remove("post")

    jsonFile(testFile) shouldBe json(
      """{
        |  "posts" : [
        |    {
        |      "id" : 1,
        |      "title" : "some post"
        |    }
        |  ]
        |}
      """.stripMargin)

    updatedDb.remove("posts")

    jsonFile(testFile) shouldBe emptyJsonObject

  }

  "Can remove values from a list" in {

  }


  "??? can create new db in /tmp with random file name" in {

  }



}

case class Db(values: Map[String, Json ] = Map(), file: Option[String] = None){

  private def save: Db = {
    file.foreach(File(_).writeAll(values.asJson.spaces2))
    this
  }

  def set[T](item: String, value: T)(implicit encoder: Encoder[T]): Db = {
    copy(values = values + (item -> value.asJson))
      .save
  }

  def addItem[T](listName: String, item: T)(implicit encoder: Encoder[T], decoder: Decoder[T]): Db = {
    val list: List[T] = get[List[T]](listName).getOrElse(List.empty[T])
    set[List[T]](listName, (item :: list).reverse)
  }

  def get[T](name: String)(implicit decoder: Decoder[T]): Option[T] = values.get(name).flatMap(_.as[T].toOption)

  def find[T](listName: String, predicate: T => Boolean)(implicit encoder: Encoder[T], decoder: Decoder[T]): Option[T] = get[List[T]](listName).flatMap(_.find(predicate))

  def remove(name: String): Db = {
    copy(values = values.filterKeys(_ != name))
    .save
  }


}
object Db {

  def initialize(file: String): Db = {
    //  def load: Db = {
    //      Db(parse(Source.fromFile(dbFileName)("UTF-8").mkString)
    //        .getOrElse(throw new RuntimeException("boom!"))
    //        .asObject.map(_.toMap)
    //        .getOrElse(Map())
    //      )
    //  }

    new Db(file = Some(file)).save
  }
  def inMemory: Db = new Db()


  //  def load: Db = {
  //      Db(parse(Source.fromFile(dbFileName)("UTF-8").mkString)
  //        .getOrElse(throw new RuntimeException("boom!"))
  //        .asObject.map(_.toMap)
  //        .getOrElse(Map())
  //      )
  //  }

  //  def set[A](itemName: String, value: A)(implicit encode: Encoder[A]): Unit =
//    File("db.json").writeAll(value.asJson.spaces2)

  //  val file = File("db.json")
  //
  //  if (!file.exists || file.length == 0){
  //    initialize
  //  }
  //
  //  def initialize: Db = {
  //    val asJson: Json = Map[String, String]().asJson
  //    file.writeAll(asJson.spaces2)
  //    this
  //  }


}

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
