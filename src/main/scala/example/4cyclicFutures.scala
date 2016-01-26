package example

import scala.concurrent.{ ExecutionContext, Promise, Future }

object Cache {
  def apply(f: Int ⇒ Future[Int])(implicit ec: ExecutionContext): Int ⇒ Future[Int] = {
    var cache = Map.empty[Int, Future[Int]]
    val lock = new Object

    key ⇒
      lock.synchronized {
        cache.get(key) match {
          // cache hit
          case Some(result) ⇒ result
          // cache miss
          case None ⇒
            val result = Promise[Int]()
            Future { // run `f` concurrently
              result.completeWith(f(key))
            }
            cache += key -> result.future
            result.future
        }
      }
  }
}

object CyclicFutures {
  import scala.concurrent.ExecutionContext.Implicits.global

  val f: Int ⇒ Future[Int] = Cache(calculateF)
  def calculateF(i: Int): Future[Int] =
    i match {
      case 42 ⇒ f(23).map(_ + 1)
      case 23 ⇒ f(42).map(_ * 1)
      case x  ⇒ Future.successful(123)
    }

  def main(args: Array[String]): Unit = {
    val sum: Future[Int] =
      for {
        aRes ← f(23)
        bRes ← f(42)
      } yield aRes + bRes

    println("Waiting for completion....")

    sum.onComplete { res ⇒
      println(s"Program was completed with $res")
      sys.exit()
    }

    // sleep while waiting for completion
    while (true) Thread.sleep(1)
  }
}
