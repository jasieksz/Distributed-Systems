package server

import akka.actor.{Actor, PoisonPill}

import scala.io.{BufferedSource, Source}

class SearchWorker extends Actor {
  def receive: Receive = {
    case "exit" =>
      println("Search Worker Suicide")
      self ! PoisonPill
    case msg: String =>
      println("Searching for price of " + msg)
      searchDatabase()
  }

  def searchDatabase() {
    val bufferedSource: BufferedSource = Source.fromFile("resources/books1")
    for (line <- bufferedSource.getLines) {
      println(line)
    }
    bufferedSource.close
  }


}
