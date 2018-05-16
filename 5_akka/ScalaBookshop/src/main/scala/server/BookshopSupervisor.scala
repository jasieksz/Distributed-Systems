package server

import akka.actor.{Actor, ActorSystem, PoisonPill}
import Util.{OrderOperation, SearchOperation}

class BookshopSupervisor extends Actor{
  def receive: Receive = {
      case "terminate" =>
        println("Supervisor Suicide")
        self ! PoisonPill
      case op: OrderOperation => println("Processing Order")
      case op: SearchOperation => println("Searching for product")
  }
}
