package client

import java.io.File

import akka.actor.{ActorSystem, Props}
import Util.{OrderOperation, SearchOperation, StreamOperation}
import com.typesafe.config.ConfigFactory

object ClientApp extends App {

  println("Client")

  val config = ConfigFactory.parseFile(new File("resources/client_application.conf"))
  val system = ActorSystem("client_system", config)
  val clientActor = system.actorOf(Props[ClientActor])

  val input = scala.io.StdIn
  var run = true

  while (run) {
    val str = input.readLine("Enter command : [search / order / read / quit]\n")
    str match {
      case "search" =>
        val title = input.readLine("Enter book title\n")
        clientActor ! SearchOperation(title, null)
      case "order" =>
        val title = input.readLine("Enter book title\n")
        clientActor ! OrderOperation(title, null)
      case "read" =>
        val title = input.readLine("Enter book title\n")
        clientActor ! StreamOperation(title, null)
      case "quit" =>
        run = false
        clientActor ! "terminate"
      case _ =>
        println("Invalid command")
    }
  }
  system.terminate()
}