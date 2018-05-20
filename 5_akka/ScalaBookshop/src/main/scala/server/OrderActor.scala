package server

import akka.actor.{Actor, PoisonPill}
import Util.{OrderOperation, Result}
import java.io._

import Util.Util._

class OrderActor extends Actor {
  val orderDbPath: String = "resources/orders"
  def receive: Receive = {
    case "terminate" =>
      println("Order Actor Suicide")
      self ! PoisonPill
    case OrderOperation(title, client) =>
      client ! makeOrder(title)

  }

  def makeOrder(title: String): Result = {
    val pw = new PrintWriter(new BufferedWriter(new FileWriter(orderDbPath, true)))
    pw.println(title)
    pw.close()
    if (pw.checkError())
      Result(ORDER_FAILED)
    else
      Result(ORDER_SUCCESS)
  }
}
