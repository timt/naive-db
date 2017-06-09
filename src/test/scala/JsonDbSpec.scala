import java.nio.file.{Files, Paths}

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser.parse
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

  def json(jsonString: String): Json = parse(jsonString).getOrElse(throw new RuntimeException(s"boom! \n===\n$jsonString\n==="))

  class TestJsonFile {
    val path: String = java.io.File.createTempFile(this.getClass.getSimpleName, ".db").getAbsolutePath

    def asJson: Json = json(Source.fromFile(path).mkString)

  }

  implicit class WithTempFile(testName: String) {
    def withTempFile(f: (TestJsonFile) => Any): Unit = testName in {
      f(new TestJsonFile)
    }
  }

  val emptyJsonObject: Json = JsonObject.empty.asJson


  "can create new db in named file" withTempFile { file =>
    Db.initialize(file.path)
    file.asJson shouldBe emptyJsonObject
  }

  "Can load db from named file" withTempFile { file =>
    File(file.path).writeAll(Map("post" -> Post(3, "some ting")).asJson.spaces2)
    Db.initialize(file.path)
      .get[Post]("post") shouldBe Some(Post(3, "some ting"))
  }


  "Can set an item value" withTempFile { file =>
    Db
      .initialize(file.path)
      .set("post", Post(1, "another new thing"))

    file.asJson shouldBe json(
      """{
        |  "post" : {
        |    "id" : 1,
        |    "title" : "another new thing"
        |  }
        |}
      """.
        stripMargin)
  }

  "Can get a value" withTempFile { file =>
    val db = Db
      .initialize(file.path)
      .set("post", Post(1, "another new thing"))

    val item: Option[Post] = db.get[Post]("post")

    item shouldBe Some(Post(1, "another new thing"))
  }


  "Can add a value to to list" withTempFile { file =>
    val db = Db
      .initialize(file.path)
      .addItem[Post]("posts", Post(1, "some post"))

    file.asJson shouldBe json(
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

    file.asJson shouldBe json(
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


  "Can find a value in a list" withTempFile { file =>
    val post = Db
      .initialize(file.path)
      .addItem[Post]("posts", Post(1, "some post"))
      .addItem[Post]("posts", Post(2, "another post"))
      .find[Post]("posts", _.title.contains("another"))

    post shouldBe Some(Post(2, "another post"))

  }


  "Can remove a value" withTempFile { file =>
    val db = Db
      .initialize(file.path)
      .set("post", Post(1, "another new thing"))
      .addItem[Post]("posts", Post(1, "some post"))

    file.asJson shouldBe json(
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
      """.
        stripMargin)

    val updatedDb = db.remove("post")

    file.asJson shouldBe json(
      """{
        |  "posts" : [
        |    {
        |      "id" : 1,
        |      "title" : "some post"
        |    }
        |  ]
        |}
      """.
        stripMargin)

    updatedDb.

      remove("posts")

    file.asJson shouldBe emptyJsonObject

  }

  "Can remove values from a list" withTempFile { file =>
    Db.initialize(file.path)
      .addItem[Post]("posts", Post(1, "some post"))
      .addItem[Post]("posts", Post(2, "another post"))
      .addItem[Post]("posts", Post(3, "yet another post"))
      .removeItem[Post]("posts", _.id == 2)
      .get[List[Post]]("posts") shouldBe Some(List(Post(1, "some post"), Post(3, "yet another post")))
  }

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
