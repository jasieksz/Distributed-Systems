package server

import akka.actor.Actor

class SearchWorker extends Actor{
  def receive: Receive = {
    case msg: String => println("Searching for price of " + msg)
  }

}
