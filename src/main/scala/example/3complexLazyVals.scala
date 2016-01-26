package example

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CyclicLazyValsComplex {
  // need to put the lazy vals into different containers, otherwise, they
  // will use the same lock
  object A {
    lazy val a: Int = {
      Thread.sleep(100)
      B.b
    }
  }
  object B {
    lazy val b: Int = {
      Thread.sleep(100)
      A.a
    }
  }

  def main(args: Array[String]): Unit = {
    val fa = Future(A.a)
    val fb = Future(B.b)

    val sum: Future[Int] =
      for {
        a ← fa
        b ← fb
      } yield a + b

    println("Waiting for completion....")
    Thread.sleep(10000)
    sum.value match {
      case Some(x) ⇒ println(s"Got result: $x")
      case None    ⇒ println("Future was never completed.")
    }
  }
}