package example

object Simple extends App {
  def a: Int = b
  def b: Int = a

  def run(): Unit = a

  run()
}