package start.basics.part4

import cats.effect.kernel.Outcome.{Succeeded, Errored, Canceled}
import cats.effect.{Fiber, IO, IOApp}
import utils_useful.extensions.*

import concurrent.duration.*

object Examples1 extends IOApp.Simple {

  /** 1. Write a function that runs an IO on another thread, and, depending on the result of the fiber:
   *  - return the result in an IO
   *  - if errored or cancelled, return a failed IO
   *  */

  def processResults[A](io: IO[A]): IO[A] = {
    // Start & run fiber
    val ioResult = for {
      fib <- io.debug.start         // start fiber. Use debug custom method to display to screen the result
      result <- fib.join
    } yield result

    // Error-handle the result of this fiber
    ioResult.flatMap {
      case Succeeded(fa) => fa
      case Errored(e) => IO.raiseError(e)
      case Canceled() => IO.raiseError(new RuntimeException("computation cancelled"))
    }
  }

  // Test exercise 1:
  def testEx1(): IO[Unit] = {
    val computation = IO("starting").debug >> IO.sleep(1.second) >> IO("done!").debug >> IO(42)     // Chain of actions (side-effects) with end result being 42
    processResults(computation).void      // void to discard end result & return IO[Unit]
  }


  /** 2. Write a function that takes 2 IOs, runs them on different fibers and returns an IO with a tuple containing both results:
   *  - if both IOs complete successfully, tuple their results
   *  - if the 1st IO returns an error, raise that error (ignoring the 2nd IO's result/error)
   *  - if the 1st IO doesnt error but the 2nd IO returns an error, raise that error
   *  - if 1 (or both) canceled, raise a RuntimeException
   *  */

  def tupleIOs[A, B](io1: IO[A], io2: IO[B]): IO[(A, B)] = {

    // Running fibers on different threads
    val results = for {
      fib1 <- io1.start
      fib2 <- io2.start
      result1 <- fib1.join
      result2 <- fib2.join
    } yield (result1, result2)      // This tuple is type (F[Outcome[F, E, A]], F[Outcome[F, E, A]]), not as IOs

    // Handling to get results into IO[(A, B)] form
    results.flatMap {
      case (Succeeded(fa), Succeeded(fb)) => for {      // tuple together the results of fa & fb: Outcome[A] & Outcome[B] ==> (a, b): IO[(a, b)]
        a <- fa
        b <- fb
      } yield (a, b)

      case (Errored(e), _) => IO.raiseError(e)        // If either IO errors, return error (dont care about the result of other IO then)
      case (_, Errored(e)) => IO.raiseError(e)

      case _ => IO.raiseError(new RuntimeException("some computation canceled"))     // If either is Canceled() -> could be (Canceled(), _) or (_, Canceled())
    }
  }

  // Test exercise 2:
  def testEx2(): IO[Unit] = {
    val firstIO = IO.sleep(2.seconds) >> IO(1).debug
    val secondIO = IO.sleep(3.seconds) >> IO(2).debug
    tupleIOs(firstIO, secondIO).debug.void
  }


  /** 3. Write a function that adds a timeout to an IO:
   *  - IO runs on a fiber
   *  - if the timeout duration passes, then the fiber is cancelled
   *  - the method returns an IO[A] which contains:
   *    - the original value if the computation is successful before the timeout signal
   *    - the exception if the computation is failed before the timeout signal
   *    - a RuntimeException if it times out (i.e. cancelled by the timeout)
   *  */

  def timeout[A](io: IO[A], duration: FiniteDuration): IO[A] = {
    val computation = for {
      fib <- io.start                           // start the thread

      // After duration passed, cancel the fiber (will only cancel if fiber still running - we set the running time of this fiber inside it's IO)
      _ <- IO.sleep(duration) >> fib.cancel             // timeout will run on current (main) thread
//      _ <- (IO.sleep(duration) >> fib.cancel).start     // timeout will run on another thread -> careful as fibers can leak (starting a fiber & not doing anything with it)

      result <- fib.join                        // get result of the fiber
    } yield result

    computation.flatMap {
      case Succeeded(fa) => fa                // If Succeeded or Errored, then fiber finished before it was canceled by timeout
      case Errored(e) => IO.raiseError(e)
      case Canceled() => IO.raiseError(new RuntimeException("computation cancelled"))     // fiber was canceled by timeout before it finished
    }
  }

  // Test exercise 3:
  def testEx3(fiberDuration: FiniteDuration, timeoutDuration: FiniteDuration): IO[Unit] = {
    val computation = IO("starting").debug >> IO.sleep(fiberDuration) >> IO("done!").debug >> IO(12)     // Chain of actions (side-effects) with end result being 42
    timeout(computation, timeoutDuration).debug.void
  }




  /** --------------------------------------------- <p>
   *  TESTING:
   *  */
  override def run: IO[Unit] = {
//    testEx1()
//    testEx2()

    testEx3(1.seconds, 2.seconds)         // fiber finishes before it gets canceled by timeout
//    testEx3(2.seconds, 1.seconds)         // fiber canceled by timeout before it finishes

  }
}
