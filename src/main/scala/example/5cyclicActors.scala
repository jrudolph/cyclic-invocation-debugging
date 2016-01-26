package example

import akka.actor.{ Stash, Props, Actor, ActorSystem }
import akka.util.Timeout

object CyclicActors extends App {
  val system = ActorSystem()

  class A extends Actor with Stash {
    val b = context.actorOf(Props(new B))

    def receive = idle

    def idle: Receive = {
      case "get" ⇒
        stash()
        b ! "get"
        context.become(calculating)
    }
    def calculating: Receive = {
      case "get" ⇒ stash()
      case value: Int ⇒
        context.become(calculated(value))
        unstashAll()
    }
    def calculated(value: Int): Receive = {
      case "get" ⇒ sender() ! value
    }
  }

  class B extends Actor with Stash {
    val a = context.parent

    def receive = idle

    def idle: Receive = {
      case "get" ⇒
        stash()
        a ! "get"
        context.become(calculating)
    }
    def calculating: Receive = {
      case "get" ⇒ stash()
      case value: Int ⇒
        context.become(calculated(value))
        unstashAll()
    }
    def calculated(value: Int): Receive = {
      case "get" ⇒ sender() ! value
    }
  }

  val a = system.actorOf(Props(new A))
  import akka.pattern.ask
  import system.dispatcher
  import scala.concurrent.duration._
  implicit val timeout = Timeout(5.seconds)
  val res = (a ? "get").mapTo[Int]

  println("Waiting for completion")
  res.onComplete(println)

  system.scheduler.scheduleOnce(10.seconds)(system.shutdown())
}