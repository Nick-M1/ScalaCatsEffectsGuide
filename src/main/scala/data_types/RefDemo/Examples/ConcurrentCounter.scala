package data_types.RefDemo.Examples

import cats.effect.{IO, IOApp, Ref, Sync}
import utils_useful.extensions.*
import cats.syntax.parallel.*
import cats.implicits._

/** CONCURRENT COUNTER: <p>
 *
 * The workers will concurrently run and modify the value of the Ref so this is one possible outcome showing “#worker » currentCount”: <br>
 * #2 >> 0 <br>
 * #1 >> 0 <br>
 * #3 >> 0 <br>
 * #1 >> 0 <br>
 * #3 >> 2 <br>
 * #2 >> 1
 *
 * */

object ConcurrentCounter extends IOApp.Simple {

  class Worker[F[_]](number: Int, ref: Ref[F, Int])(implicit F: Sync[F]) {

    // prints value
    private def putStrLn(value: String): F[Unit] =
      F.delay(println(value))

    // Start this worker
    def start: F[Unit] = for {
      c1 <- ref.get // gets & displays current value in ref
      _ <- putStrLn(show"#$number >> $c1")
      c2 <- ref.modify(x => (x + 1, x)) // modifies & displays current value in ref
      _ <- putStrLn(show"#$number >> $c2")
    } yield ()
  }

  val program: IO[Unit] = for {
    ref <- Ref[IO].of(0) // Initiate Ref with value 0
    w1 = new Worker[IO](1, ref) // Initiate 3 workers
    w2 = new Worker[IO](2, ref)
    w3 = new Worker[IO](3, ref)

    _ <- List( // Start the 3 workers in parallel
      w1.start,
      w2.start,
      w3.start
    ).parSequence.void
  } yield ()


  override def run: IO[Unit] = program

}
