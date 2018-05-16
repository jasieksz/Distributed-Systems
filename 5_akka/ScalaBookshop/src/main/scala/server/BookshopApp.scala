package server

import akka.actor.{ActorSystem, PoisonPill, Props}

object BookshopApp extends App{
  print("Bookshop")
  val system: ActorSystem = ActorSystem("bookShop")
  val orderActor = system.actorOf(Props(new OrderActor))
  val searchWorker = system.actorOf(Props(new SearchWorker))

  val input = scala.io.StdIn
  while(true){
    var str = input.readLine()
    str match {
      case "search" => searchWorker ! "chleb"
      case "order" => orderActor ! "maslo"
      case "quit" => {
        orderActor ! PoisonPill
        searchWorker ! PoisonPill
      }
    }

  }


}
