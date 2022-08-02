package data_types.ClockDemo

import cats.effect._
import cats.syntax.all._
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration._

object Example1 extends App {

  // Measure time taken for process fa to finish:
  def measure[F[_], A](fa: F[A])(implicit F: Sync[F], clock: Clock[F]): F[(A, FiniteDuration)] = for {
    start <- clock.monotonic          // start timer
    result <- fa                      // run process
    finish <- clock.monotonic         // end timer
  } yield (result, finish - start)    // return the result from process && the time taken


  def process(): IO[Unit] = {
    IO(println("Process start")) >>
      IO.sleep(5.seconds) >>
      IO.println("Process end")
  }


  println(
    measure(process()).unsafeRunSync()
  )
  // Process start
  // Process end
  // ((),5031001600 nanoseconds)

}
