package client

import akka.actor.{Actor, ActorRef, ActorSelection, PoisonPill}
import Util.{OrderOperation, Result, SearchOperation}

class ClientActor extends Actor{
  val bookshopSupervisor: ActorSelection = context.actorSelection("akka.tcp://bookshop_system@127.0.0.1:2552/user/master")

  def receive: Receive = {
    case "terminate" =>
      self ! PoisonPill

    case Result(value) =>
      println("Received result: " + value)
    case SearchOperation(title,_) =>
      bookshopSupervisor ! SearchOperation(title, self)
    case OrderOperation(title,_) =>
      bookshopSupervisor ! OrderOperation(title, self)
  }
}
