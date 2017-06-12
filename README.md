naive-db  [![Build Status](https://travis-ci.org/timt/naive-db.png?branch=master)](https://travis-ci.org/timt/naive-db) [ ![Download](https://api.bintray.com/packages/timt/repo/naive-db/images/download.png) ](https://bintray.com/timt/repo/naive-db/_latestVersion)
===========================================================================================================================================================================================================================================================================================
A really simple json db library implemented in scala. 

Requirements
------------

* [scala](http://www.scala-lang.org) 2.12.1
* [scala](http://www.scala-lang.org) 2.11.2

Usage
-----
Add the following lines to your build.sbt

    resolvers += "Tim Tennant's repo" at "http://dl.bintray.com/timt/repo/"

    libraryDependencies += "io.shaka" %% "naive-http" % "94"

and Circe for encoding/decoding json

    val circeVersion = "0.8.0"  //Change this to prefered version
    libraryDependencies ++=Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion
    )
    
Start hacking

    import io.shaka.db.Db
    import io.circe._
    import io.circe.generic.semiauto._
    import io.circe.parser.parse
    import io.circe.syntax.EncoderOps

     case class Post(id: Int, title: String)
    
      object Post {
        implicit val decoder: Decoder[Post] = deriveDecoder[Post]
        implicit val encoder: Encoder[Post] = deriveEncoder[Post]
      }
      
      val db = Db.initialize("./db.json") //creates/loads the file ./db.json
      
      //Set an item/object in database
      db.set("post", Post(1, "another new thing"))
      
      //Get an item/object from database
      val item: Option[Post] = db.get[Post]("post")
      
      //Add items to a list in the database
      db
        .addItem[Post]("posts", Post(1, "some post"))
        .addItem[Post]("posts", Post(2, "another post"))
        
      //Find and item in a list
      val post: Option[Post] = Db.find[Post]("posts", _.title.contains("another"))
      
      //Remove an item/object
      db.remove("post")
      db.remove("posts")
      
      //Remove and item from a list
      db.removeItem[Post]("posts", _.id == 2)

For more examples see 

* [DbSpec.scala](https://github.com/timt/naive-db/blob/master/src/test/scala/io/shaka/db/DbSpec.scala)
    
Code license
------------
Apache License 2.0