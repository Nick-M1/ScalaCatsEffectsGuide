package start.basics.part2_IOBasics

import cats.effect.kernel.Outcome.{Errored, Succeeded}
import utils_useful.extensions.*
import cats.effect.{IO, IOApp, Outcome, OutcomeIO}
import cats.effect.unsafe.implicits.global
import cats.syntax.parallel.*
import cats.syntax.apply.*

object IO_Intro4_HigherLevelFunctions extends IOApp.Simple {

  val io1: IO[Int] = IO(5)
  val io2: IO[Int] = IO(6)
  val io3: IO[Unit] = IO.canceled


  // COMBINING IOs IN SEQUENCE:

  // Runs io1 & io2 in sequence, then passes both their results as arguments to a function (_+_)
  val res1: IO[Int] = (io1, io2).mapN(_ + _)        // 11




  // COMBINING IOs IN PARALLEL:

  // Same as mapN, but runs each IO in parallel (not sequentially like mapN)
  val res2: IO[Int] = (io1, io2).parMapN(_ + _)     // 11

  // Runs io1 & io2 in parallel, then returns their results as a tuple
  val res3: IO[(Int, Int)] = (io1, io2).parTupled   // (5, 6)
  val res4: IO[(Int, Int)] = io1.both(io2)          // (5, 6)

  val res5: IO[(OutcomeIO[Int], OutcomeIO[Int])] = io1.bothOutcome(io2)   // (Succeeded(IO(5)),Succeeded(IO(6))) -> Easy for error-handling

  // res5 extended - using error handling depending on outcome of bothOutcome
  val res6: IO[(Int, Int)] = io1.bothOutcome(io2).flatMap {
    case (Succeeded(fa), Succeeded(fb)) => for {
                                            a <- fa
                                            b <- fb
                                          } yield (a, b)

    case (Errored(_), _) | (_, Errored(_))  => throw new RuntimeException()
    case _ => throw new RuntimeException()
  }



  override def run: IO[Unit] = {
    res6.debug.void
  }


}
