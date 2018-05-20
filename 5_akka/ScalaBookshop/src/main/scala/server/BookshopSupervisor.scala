package server

import java.io.IOException

import akka.actor.{Actor, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import Util.{OrderOperation, SearchOperation, StreamOperation}
import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.routing.RoundRobinPool

import scala.concurrent.duration.DurationInt
import scala.concurrent.Future

class BookshopSupervisor extends Actor {
  val router: ActorRef = context.actorOf(RoundRobinPool(4).props(Props[SearchWorker]), "router")
  val orderActor: ActorRef = context.actorOf(Props[OrderActor], "order")

  def receive: Receive = {
    case "terminate" =>
      println("Supervisor Suicide")
      orderActor ! "terminate"
      router ! PoisonPill
      self ! PoisonPill
    case OrderOperation(title, client) =>
      println("Bookshop received order for: " + title)
      orderActor ! OrderOperation(title, client)
    case SearchOperation(title, client) =>
      println("Bookshop received search query: " + title)
      router ! SearchOperation(title, client)
    case StreamOperation(title, client) =>
      println("Bookshop received stream request: " + title)
      val streamingActor: ActorRef = context.actorOf(Props[StreamingActor])
      streamingActor ! StreamOperation(title, client)
  }

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case _: IOException => Restart
      case _: Exception => Escalate
    }
}
