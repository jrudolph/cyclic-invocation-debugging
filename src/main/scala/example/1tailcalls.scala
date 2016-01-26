package example

object TailCalls {
  def f(argument: Int): Int =
    argument match {
      case 42 ⇒ f(argument - 19)
      case 23 ⇒ f(argument * 2 - 4)
      case x  ⇒ x + 5
    }

  def main(args: Array[String]): Unit = {
    f(0) // everything's fine
    f(42)
  }
}