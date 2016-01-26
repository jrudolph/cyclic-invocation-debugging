package example

object CyclicLazyValsSimple extends App {
  object Test {
    lazy val a: Int = b
    lazy val b: Int = a
  }

  Test.a
}
