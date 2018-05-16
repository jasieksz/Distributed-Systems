package server

import akka.actor.Actor

class OrderActor extends Actor{
  def receive: Receive = {
    case msg: String => println("Received order for " + msg)
  }
}
