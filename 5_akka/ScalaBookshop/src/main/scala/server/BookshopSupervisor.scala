package server

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import Util.{OrderOperation, SearchOperation}
import akka.routing.RoundRobinPool

import scala.concurrent.Future

class BookshopSupervisor extends Actor{
  val router: ActorRef = context.actorOf(RoundRobinPool(4).props(Props[SearchWorker]), "router")
  val orderActor: ActorRef = context.actorOf(Props[OrderActor], "order")

  def receive: Receive = {
      case "terminate" =>
        println("Supervisor Suicide")
        orderActor ! "terminate"
        router ! PoisonPill
        self ! PoisonPill
      case OrderOperation(title, client) =>
        println("BS order: " + title)
        orderActor ! OrderOperation(title, client)
      case SearchOperation(title, client) =>
        println("BS search: " + title)
        router ! SearchOperation(title, client)

  }
}
