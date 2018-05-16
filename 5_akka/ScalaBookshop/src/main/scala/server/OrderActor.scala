package server

import akka.actor.{Actor, PoisonPill}

class OrderActor extends Actor {
  def receive: Receive = {
    case "exit" =>
      println("Order Actor Suicide")
      self ! PoisonPill
    case msg: String => println("Received order for " + msg)
  }
}
