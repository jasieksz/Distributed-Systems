package client

import Util.Util._
import akka.actor.{Actor, ActorSelection, PoisonPill}
import Util.{OrderOperation, Result, SearchOperation, StreamOperation}

class ClientActor extends Actor{
  val bookshopSupervisorPath: String = "akka.tcp://bookshop_system@127.0.0.1:2552/user/master"
  val bookshopSupervisor: ActorSelection = context.actorSelection(bookshopSupervisorPath)

  def receive: Receive = {
    case "terminate" =>
      self ! PoisonPill

    case Result(value) => value match {
        case NO_BOOK => println("Book not found")
        case ORDER_SUCCESS => println("Order confirmed")
        case ORDER_FAILED => println("Order failed, try again")
        case _ => println("Price is : " + value)
      }
    case SearchOperation(title,_) =>
      bookshopSupervisor ! SearchOperation(title, self)
    case OrderOperation(title,_) =>
      bookshopSupervisor ! OrderOperation(title, self)
    case StreamOperation(title, _) =>
      bookshopSupervisor ! StreamOperation(title, self)
    case msg: String =>
      println(msg)
//      if (msg.startsWith("Completed:")){
//        val id: Char = msg.charAt(msg.length-1)
////        context.actorSelection(bookshopSupervisorPath + "/$" + id) ! "terminate"
//      } else {
//        println(msg)
//      }
  }
}
