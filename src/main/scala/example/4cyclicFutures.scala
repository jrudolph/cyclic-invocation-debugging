package example

object CyclicFutures {
  import scala.concurrent.{ Promise, Future }
  import scala.concurrent.ExecutionContext.Implicits.global

  def futurePlus1(underlying: ⇒ Future[Int]): Future[Int] = {
    // we make a few verrenkungen to avoid evaluating `underlying`
    // too eagerly
    val p = Promise[Int]()
    Future {
      Thread.sleep(100)
      p.completeWith(underlying)
    }
    p.future
  }

  object Results {
    val a: Future[Int] = futurePlus1(b)
    val b: Future[Int] = futurePlus1(a)
  }

  def main(args: Array[String]): Unit = {
    // start computations by initializing `Results`
    Results

    val sum: Future[Int] =
      for {
        a ← Results.a
        b ← Results.b
      } yield a + b

    println("Waiting for completion....")

    sum.onComplete { res ⇒
      println(s"Program was completed with $res")
      sys.exit()
    }

    // sleep while waiting for completion
    while (true) Thread.sleep(1)
  }
}
