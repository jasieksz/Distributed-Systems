package Util

import akka.actor.ActorRef

sealed trait Operation
case class SearchOperation(title: String, client: ActorRef) extends Operation
case class OrderOperation(title: String, client: ActorRef) extends Operation
case class StreamOperation(title: String, client: ActorRef) extends Operation
case class Result(value: Int) extends Operation


object Util {
  val NO_BOOK: Int = 0
  val ORDER_SUCCESS: Int = -1
  val ORDER_FAILED: Int = -2
}
