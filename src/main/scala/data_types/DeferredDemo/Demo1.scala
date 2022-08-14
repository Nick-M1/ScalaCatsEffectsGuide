package data_types.DeferredDemo

import cats.effect.{Deferred, IO, IOApp}
import cats.syntax.all.*
import cats.effect.unsafe.implicits.global

// Using Cats-effects:
object Demo1 extends IOApp.Simple {

  /** ONLY ONCE Scenario: <p>
   *  Many processes can modify the same value but you only care about the first one in doing so and stop processing, then use Deferred[F, A]. <p>
   *  Two processes will try to complete at the same time but only one will succeed, completing the deferred primitive exactly once.
   *  The loser one will raise an error when trying to complete a deferred already completed and automatically be canceled by the IO.race mechanism, that’s why we call attempt on the evaluation.
   * */
  def start(d: Deferred[IO, Int]): IO[Unit] = {
    val attemptCompletion: Int => IO[Unit] = n => d.complete(n).attempt.void    // a function, given an integer then returns an IO with deferred being set to this integer

    List(
      IO.race(attemptCompletion(1), attemptCompletion(2)),      // Runs 2 fibers (in parallel), which both attempt to set the value in deferred (but only 1 fiber will be able to set it)
      d.get.flatMap { n => IO(println(show"Result: $n")) }      // Gets the value that Deferred is now (after it has been set)
    ).parSequence.void                                          // Runs (sets & then gets)
  }

  val program: IO[Unit] = for {
    d <- Deferred[IO, Int]        // Initialises Deferred (as empty)
    _ <- start(d)                 // Runs process to set & then get the value in deferred
  } yield ()

  override def run: IO[Unit] =
    program
}

