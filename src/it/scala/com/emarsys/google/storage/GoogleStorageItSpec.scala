package com.emarsys.google.storage

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class GoogleStorageItSpec extends WordSpec with Matchers with ScalaFutures {

  implicit val system = ActorSystem("google-storage-stream")
  implicit val materializer = ActorMaterializer()

  val testConfig = system.settings.config getConfig "googleStorage"
  val testBucket = testConfig.getString("project.bucket")
  val testProject = testConfig.getString("project.name")
  val googleService = GoogleStorageService(testProject)


  "Read file from storage" should {

    "default use chunk size which is 64" in {
      val list = mutable.MutableList.empty[Int]
        val testFile= getClass.getResourceAsStream("/empty64k")
      googleService.get(testBucket).create("test64",testFile)

      Await.result(GoogleStorage.storageSource("test64").runForeach(element => {
            list += element.size
      }), 3.seconds)

      list.size shouldBe 1
      list.head shouldBe 64*1024
    }

    "be able to set chunk size to 1" in {
      val list = mutable.MutableList.empty[Int]
      val testFile= getClass.getResourceAsStream("/empty64k")
      googleService.get(testBucket).create("test1",testFile)

      Await.result(GoogleStorage.storageSource("test1", 1).runForeach(element => {
        list += element.size
      }), 3.seconds)

      list.size shouldBe 64
      list.head shouldBe 1024
    }

    "use default chunk size when size set to 0" in {
      val list = mutable.MutableList.empty[Int]
      val testFile= getClass.getResourceAsStream("/empty64k")
      googleService.get(testBucket).create("test1",testFile)

      Await.result(GoogleStorage.storageSource("test1", 0).runForeach(element => {
        list += element.size
      }), 3.seconds)

      list.size shouldBe 1
      list.head shouldBe 64*1024
    }

    "read file with content" in {
      val list = mutable.MutableList.empty[String]
      val testFile= getClass.getResourceAsStream("/test_content")
      googleService.get(testBucket).create("test_content",testFile)

      Await.result(GoogleStorage.storageSource("test_content", 1).runForeach(element => {
        list += element.utf8String
      }), 3.seconds)

      list.size shouldBe 1
      list.head shouldBe "Content is here."
    }
  }

  "Check if file exists in storage" when {

    "file does not exist" should {
      "return false" in {
        GoogleStorage.checkFile("foo") shouldBe false
      }
    }

    "file exists" should {
      "return true" in {
        val testFile= getClass.getResourceAsStream("/test_content")
        googleService.get(testBucket).create("test_content",testFile)

        GoogleStorage.checkFile("test_content") shouldBe true
      }
    }

  }

  "Signed url for file name and duration" when {

    "file does not exist" should {
      "return none" in {
        GoogleStorage.signedUrlFor("non_existing_file", 5.seconds) shouldBe None
      }
    }

    "file exists" should {
      "return properly signed url" in {
        val testFileName = "test_content"
        val testFile = getClass.getResourceAsStream("/" + testFileName)
        googleService.get(testBucket).create(testFileName, testFile)

        val Some(url) = GoogleStorage.signedUrlFor(testFileName, 5.seconds)
        url.getPath should endWith(testFileName)
        url.getQuery should include("Expires")
        url.getQuery should include("Signature")
      }
    }

  }

}
