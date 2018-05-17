package server

import java.io.File

import Util.{OrderOperation, SearchOperation}
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object BookshopApp extends App {
  print("Bookshop\n ")

//  val configFile = getClass.getClassLoader
//    .getResource("bookshop_application.conf")
//    .getFile
  val config = ConfigFactory.parseFile(new File("resources/bookshop_application.conf"))
  val system: ActorSystem = ActorSystem("bookshop_system", config)
  val bookshopSupervisor = system.actorOf(Props[BookshopSupervisor], "master")


  val input = scala.io.StdIn
  var run = true

  while (run) {
    input.readLine("Enter command : [quit]\n") match {
      case "quit" =>
        run = false
        bookshopSupervisor ! "terminate"
    }
  }
  system.terminate()
}


