package server

import akka.actor.{Actor, PoisonPill}
import Util.{SearchOperation, Result}
import scala.io.{BufferedSource, Source}

class SearchWorker extends Actor {
  def receive: Receive = {
    case "terminate" =>
      println("Search Worker Suicide")
      self ! PoisonPill
    case SearchOperation(title, client) =>
      println("Searching for price of " + title)
      client ! Result(searchDatabase(title))
  }

  def searchDatabase(title: String): Int = {
    val bufferedSource: BufferedSource = Source.fromFile("resources/books1")
    val book = bufferedSource.getLines().find(line => line.startsWith(title)).getOrElse("a;0")
    bufferedSource.close
    book.split(";")(1).toInt
  }
}
