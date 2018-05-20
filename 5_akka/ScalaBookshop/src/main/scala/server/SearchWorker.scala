package server

import Util.Util.NO_BOOK
import akka.actor.{Actor, ActorRef, Kill, PoisonPill, Props}
import Util.{Result, SearchOperation}

import scala.concurrent.{Await, Future}
import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import math.max

class SearchWorker extends Actor {
  val dbPath1: String = "resources/databases/books1"
  val dbPath2: String = "resources/databases/books2"

  def receive: Receive = {
    case "terminate" =>
      println("Search Worker Suicide")
      self ! PoisonPill
    case SearchOperation(title, client) =>
      println("Searching for price of " + title)
      searchTwoDatabases(title, client)

  }

  def searchTwoDatabases(title: String, client: ActorRef): Unit = {
    val result1 = searchDatabase(dbPath1, title)
    val result2 = searchDatabase(dbPath2, title)
    val result = for {
      r1 <- result1
      r2 <- result2
    } yield max(r1, r2)

    result.onComplete {
      case Success(value) =>
        client ! Result(value)
      case Failure(e) => e.printStackTrace()
    }
  }

  def searchDatabase(path: String, title: String): Future[Int] = Future {
    val bufferedSource: BufferedSource = Source.fromFile(path)
    val book = bufferedSource.getLines().find(line => line.split(";")(0).equals(title)).getOrElse("a;"+NO_BOOK.toString)
    bufferedSource.close
    book.split(";")(1).toInt
  }
}



