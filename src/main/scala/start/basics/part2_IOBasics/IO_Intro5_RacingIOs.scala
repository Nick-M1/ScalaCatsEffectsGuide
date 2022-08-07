package start.basics.part2_IOBasics

import cats.effect.{ExitCode, Fiber, FiberIO, IO, IOApp}
import concurrent.duration._
import utils_useful.extensions.*

object IO_Intro5_RacingIOs extends IOApp.Simple {

  // Thread process #1:
  val process1: IO[Int] =
    IO("task: starting").debug *>
      IO.sleep(3.seconds) *>
      IO("task: Completed").debug *>
      IO(42)

  // If process1 gets cancelled, this gets called
  val process1Wrap: IO[Int] =
    process1.onCancel(IO("task: cancelled").debug.void)


  // Thread process #2:
  val timeout: IO[Unit] =
    IO("timeout: starting").debug *>
      IO.sleep(500.millis) *>
      IO("timeout: finished").debug.void


  // Racing: start process1 & timeout processes. The process that finishes 1st will have its result returned && the loser fiber will be canceled
  def testRace(): IO[String] = {
    val firstIO: IO[Either[Int, Unit]] = IO.race(process1Wrap, timeout)       // type: IO[ Either[ process1Type, process2Type ]]    where process2 = timeout

    firstIO.flatMap {
      case Left(v) => IO(s"task won: $v")
      case Right(_) => IO("timeout won")
    }
  }


  // BUILT-IN TIMEOUT: ==================================
  // Same as timeout() & testRace() methods above, but throws exception if timeout finishes before process1
  val timeout2: IO[Int] = process1Wrap.timeout(500.millis)


  // RACEPAIR: ======================================================
  // Similar to race(), but the slower thread doesn't get cancelled when the faster thread finishes (returns fibre of slower thread for us to deal with)
  // racePair returns IO[ Either[ (OutcomeIO[A], FiberIO[B]), (OutcomeIO[B], FiberIO[A]) ] ]   -> Returns a tuple where the finished-faster thread is returned as an OutcomeIO and the unfinished-slower thread is returned as FiberIO
  def testRacePair [A] (iox: IO[A], ioy: IO[A]): IO[Any] = {
    val pair = IO.racePair(iox, ioy)    // IO[ Either[ (OutcomeIO[A], FiberIO[B]), (FiberIO[A], OutcomeIO[B]) ] ]

    pair.flatMap {
      case Left((outcomeA, fiberB)) => fiberB.cancel *> IO("first task won").debug *> IO(outcomeA).debug
      case Right((fiberA, outcomeB)) => fiberA.cancel *> IO("second task won").debug *> IO(outcomeB).debug
    }
  }


  def run: IO[Unit] = {
    //    val a: IO[Int] = IO(42)
    //    a.debug.void        // run method output simplified due to IOApp.Simple

    //    testRace().debug.void

    // BUILT-IN TIMEOUT:
    //    timeout2.debug.void

    // RACE-PAIR:
    val iox = IO.sleep(1.seconds).as(1).onCancel(IO("first cancelled").debug.void)
    val ioy = IO.sleep(2.seconds).as(2).onCancel(IO("second cancelled").debug.void)

    testRacePair(iox, ioy).void

  }

}
