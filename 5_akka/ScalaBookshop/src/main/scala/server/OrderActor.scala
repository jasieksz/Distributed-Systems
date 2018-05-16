package server

import akka.actor.{Actor, PoisonPill}
import Util.{OrderOperation, Result}

class OrderActor extends Actor {
  def receive: Receive = {
    case "terminate" =>
      println("Order Actor Suicide")
      self ! PoisonPill
    case OrderOperation(title, client) =>
      client ! makeOrder(title)

  }

  def makeOrder(title: String): String = {
    println("Putting order for " + title + " in database")
    // TODO : write to orders file, return order made message on success
    "order made"
  }
}
