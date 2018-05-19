package server

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import Util.{Result, SearchOperation}

import scala.io.{BufferedSource, Source}

class SearchWorker extends Actor {
  val dbPath1: String = "resources/books1"
  val dbPath2: String = "resources/books2"

  def receive: Receive = {
    case "terminate" =>
      println("Search Worker Suicide")
      self ! PoisonPill
    case SearchOperation(title, client) =>
      println("Searching for price of " + title)
      val worker1: ActorRef = context.actorOf(Props[InternalSearchWorker])
      val worker2: ActorRef = context.actorOf(Props[InternalSearchWorker])
      worker1 ! InternalSearchOperation(dbPath1, title, client)
      worker2 ! InternalSearchOperation(dbPath2, title, client)
    case InternalResult(price, client) =>
      client ! Result(price)



  }



  class InternalSearchWorker extends Actor{
    def receive: Receive = {
      case InternalSearchOperation(path, title, client) =>
        sender() ! InternalResult(searchDatabase(path, title), client)
    }

    def searchDatabase(path: String, title: String): Int = {
      val bufferedSource: BufferedSource = Source.fromFile(path)
      val book = bufferedSource.getLines().find(line => line.startsWith(title)).getOrElse("a;0")
      bufferedSource.close
      book.split(";")(1).toInt
    }
  }

  case class InternalSearchOperation(path: String, title: String, client: ActorRef)
  case class InternalResult(price: Int, client: ActorRef)
}


