import akka.actor.{ActorSystem, Props}
import server.BookshopSupervisor
import Util.{SearchOperation, OrderOperation}

object BookshopApp extends App{
  print("Bookshop")
  val system: ActorSystem = ActorSystem("bookShop")
  val bookshopSupervisor = system.actorOf(Props(new BookshopSupervisor))


  val input = scala.io.StdIn
  var run = true
  while(run){
    val str = input.readLine("Enter command : [search / order / quit]\n")
    str match {
      case "search" =>
        val title = input.readLine("Enter book title\n")
        bookshopSupervisor ! SearchOperation(title)
      case "order" =>
        val title = input.readLine("Enter book title\n")
        bookshopSupervisor ! OrderOperation(title)
      case "quit" =>
        run = false
        bookshopSupervisor ! "terminate"
    }
  }
  system.terminate()

}


