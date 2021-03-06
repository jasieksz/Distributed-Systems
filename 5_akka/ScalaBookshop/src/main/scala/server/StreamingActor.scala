package server

import java.io.File

import Util.StreamOperation
import akka.{Done, NotUsed}
import akka.actor.Status.Success
import akka.actor.{Actor, ActorRef, PoisonPill}
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.Framing.delimiter
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.duration.DurationInt
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class StreamingActor extends Actor{

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val baseBooksPath: String = "resources/stream_books/"

  def receive: Receive = {
    case "terminate" =>
      println("StreamingActor Suicide")
      self ! PoisonPill
    case StreamOperation(title, client) =>
      readBook(baseBooksPath + title, client).run()
  }

  def readBook(path: String, client: ActorRef): RunnableGraph[Future[IOResult]] = {
    val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(new File(path).toPath)
    val sink: Sink[Any, NotUsed] = Sink.actorRef(client, "Completed Stream")
    // source requires empty line at the end to sen onCompleteMessage

    val newLineFlow: Flow[ByteString, String, NotUsed] = delimiter(ByteString("\n"), Int.MaxValue)
              .map(_.decodeString("UTF-8"))
              .throttle(1, 1.second)

    val newSentenceFlow: Flow[ByteString, String, NotUsed] = delimiter(ByteString("."), Int.MaxValue)
              .map(_.decodeString("UTF-8"))
              .map(_.trim())
              .throttle(1, 1.second)

    source.via(newLineFlow).to(sink)
  }

}
