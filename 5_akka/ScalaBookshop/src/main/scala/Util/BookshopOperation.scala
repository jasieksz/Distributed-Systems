package Util

import akka.actor.ActorRef

sealed trait Operation
case class SearchOperation(title: String, client: ActorRef) extends Operation
case class OrderOperation(title: String, client: ActorRef) extends Operation
case class Result(value: Int) extends Operation


