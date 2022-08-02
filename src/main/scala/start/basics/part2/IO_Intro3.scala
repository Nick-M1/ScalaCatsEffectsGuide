package start.basics.part2

import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import utils_useful.extensions.*
import cats.effect.{IO, IOApp}
import cats.effect.unsafe.implicits.global
import cats.instances.parallel.*
import cats.syntax.parallel.*

import concurrent.duration.*


object IO_Intro3 extends IOApp.Simple {

  // IOs:
  val io1: IO[Int] = IO.pure(42)
  val io2: IO[String] = IO.pure("Scala")

  // FIBERS:
  trait myFiber[F[_], A] {
    def join: F[A]          // Waits for this fiber's final result (wait to finish)
    def cancel: F[Unit]     // Cancels this fiber
  }

  // Running a single fiber on another thread
  def singleFiber(): IO[Unit] = for {
    fiber <- io1.debug.start
    _ <- fiber.join
  } yield ()




  // SINGLE-THREAD ==========================================================

  // Running the 2 processes io1 & o2 on the same (main) thread
  def sameThread(): IO[Unit] = for {
    _ <- io1.debug
    _ <- io2.debug
  } yield ()


  // CONCURRENCY (MULTI-THREAD) =============================================


  /** Runs processes io1 & io2 on different threads: */

  // Using for-comp and manually starting & joining fibers (bad & manual)
  def differentThread1(): IO[Unit] = for {
    f1 <- io1.debug.start         // io1 & io2 runs on new user-created fibers
    f2 <- io2.debug.start

    _ <- f1.join                  // Joins (forks) these 2 fibers together
    _ <- f2.join
  } yield ()

  // Using higher-level function (Better & less manual)
  def differentThread2(): IO[Unit] = {
    (io1.debug, io2.debug).parTupled.debug.void       // parTupled: runs each IO in tuple in parallel & puts their resulting values in a tuple
  }                                                   // void: dont need this final tuple (already printing it via debug method)



  /** Running a single process on another thread
   *  3 Outcomes of thread:
   *   - Succeeded( IO( value) )
   *   - Errored( exception )
   *   - Cancelled
   * */
  // 1# - Runs on another thread, and succeeds
  def runOnAnotherThread[A](io: IO[A]): IO[Any] = for {
    fib <- io.start       // Starting the thread
    result <- fib.join    // .join -> waits for thread to finish
  } yield result

  // 2# - Runs on another thread, but returns exception
  def throwOnAnotherThread(): IO[Any] = for {
    fib <- IO.raiseError[Int](new RuntimeException("MY MESSAGE")).start
    result <- fib.join
  } yield result

  // 3# - Runs on thread, but is cancelled during the process
  def cancelledThread(): IO[Any] = {
    val task = IO("starting").debug *> IO.sleep(1.second) *> IO("done").debug
    // *> = sequencing (uses flatMap). So the thread displays "starting" -> sleeps for 1 sec -> displays "done"

    for {
      fib <- task.start                                       // start thread
      _ <- IO.sleep(500.millis) *> IO("cancelling").debug     // sleep for 0.5sec, then display "cancelling"
      _ <- fib.cancel                                         // Cancel thread
      result <- fib.join                                      // Wait for thread to finish
    } yield result
  }


  // Error handling, based on thread's outcome
  def errorHandle[A](io: IO[A]): IO[Any] = io.flatMap {
    case Succeeded(effect) => IO(effect)
    case Errored(e) => IO(-1)          // must return an IO, as using flatMap
    case Canceled() => IO(-2)
  }


  override def run: IO[Unit] = {
    /* Same thread: */
    sameThread()

    /* Multi-thread: */
//    differentThread1()
//    differentThread2()

    /* Testing thread outcomes on Multi-thread: */
//    runOnAnotherThread(io1).debug.void        // Succeeded(IO(42))
//    throwOnAnotherThread().debug.void         // Exception thrown
//    cancelledThread().debug.void              // Canceled()

    /* With error handling */
//    errorHandle(runOnAnotherThread(io1)).debug.void
//    errorHandle(throwOnAnotherThread()).debug.void
//    errorHandle(cancelledThread()).debug.void




  }
}
