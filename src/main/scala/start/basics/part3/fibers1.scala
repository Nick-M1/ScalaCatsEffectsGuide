package start.basics.part3

import cats.effect.{IO, IOApp, Fiber}
import utils_useful.extensions.*

/** IO threads usually run sequentially (one after another), unless stated to run parallel */
object fibers1 extends IOApp.Simple {

  // IOs:
  val io1: IO[Int] = IO.pure(42)
  val io2: IO[String] = IO.pure("Scala")


  // Create a Fiber - Just for demonstration, should use higher level methods Cats-effects gives
  def createFiber: Fiber[IO, Throwable, String] = ???   // Almost impossible to create Fibers manually

  val fiber1: IO[Fiber[IO, Throwable, Int]] = io1.debug.start





  override def run: IO[Unit] = {
    ???
  }
}
