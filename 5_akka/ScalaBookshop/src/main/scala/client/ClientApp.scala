package client

import akka.actor.{ActorSystem, Props}
import Util.{OrderOperation, SearchOperation}

object ClientApp extends App {

  println("Client")
  val system = ActorSystem("client")
  val clientActor = system.actorOf(Props[ClientActor])

  val input = scala.io.StdIn
  var run = true

  while (run) {
    val str = input.readLine("Enter command : [search / order / quit]\n")
    str match {
      case "search" =>
        val title = input.readLine("Enter book title\n")
        clientActor ! SearchOperation(title, null) // TODO : get clients from ClientApp
      case "order" =>
        val title = input.readLine("Enter book title\n")
        clientActor ! OrderOperation(title, null)
      case "quit" =>
        run = false
        clientActor ! "terminate"
    }
  }
  system.terminate()
}