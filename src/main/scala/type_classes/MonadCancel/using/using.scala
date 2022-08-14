package type_classes.MonadCancel.using

import cats.effect.kernel.Outcome.{Errored, Succeeded, Canceled}
import cats.effect.{IO, IOApp, MonadCancel}
import utils_useful.extensions.*

object using extends IOApp.Simple {

  
  // monadCancel for IO
  val monadCancelIO: MonadCancel[IO, Throwable] = MonadCancel[IO]

  // create values with MonadCancel
  val mc1: IO[Int] = MonadCancel[IO].pure(42)
  val mc1b: IO[Int] = IO(42) // Simpler syntax that mc1,  mc1 == mc1b

  val mc2: IO[Int] = mc1.map(_ + 5) // IO(47)
  val mc3: IO[Int] = mc1.flatMap(value => IO(value + 10)) // IO(52)

  /** By default, uses MonadCancel's cancelable method when using apply. <br>
   * So, the IO can be canceled when it runs this code. <p>
   *
   * Can also use MonadCancel's uncancelable, so that the IO can't be canceled when running this code
   * */
  val mc4: IO[Int] = MonadCancel[IO].uncancelable { _ => // _ as there is no value in the MonadCancel already
    for {
      _ <- MonadCancel[IO].pure("The code in here cant be cancelled")
      res <- MonadCancel[IO].pure(56)
    } yield res
  } // IO[Int] = IO(56)

  // Same as mc4, but in a function

  import cats.syntax.flatMap.*
  import cats.syntax.functor.*

  def uncancelableFunc[F[_], E](using mc: MonadCancel[F, E]): F[Int] = mc.uncancelable { _ =>
    for {
      _ <- mc.pure("The code in here cant be cancelled")
      res <- mc.pure(76)
    } yield res
  }


  // Cancellation & Error listener for mc4:

  import cats.effect.syntax.monadCancel.*

  val mc4Listener1: IO[Int] = mc4.onCancel(IO("mc4 canceled").debug.void) // if mc4 gets canceled, the onCancel method runs
  val mc4Listener2: IO[Int] = mc4.onError(err => IO(s"mc4 errored: $err").debug.void) // if mc4 throws error, the onError method runs


  // Finalisers - actions that run after the IO has finished, no matter what the outcome of the IO is
  val mc1Finaliser1: IO[Int] = mc1.guarantee(IO("Closing mc1 resource...").debug.void)

  val mc1Finaliser2: IO[Int] = mc1.guaranteeCase {
    case Succeeded(fa) => fa.flatMap(a => IO(s"successful: $a").void)
    case Errored(e) => IO(s"failed: $e").void
    case Canceled() => IO("canceled").void
  }


  // Bracket pattern is specific to MonadCancel:
  /** Bracket needs 3 arguments (1st arg is the IO that the bracket method is being called on:
   *  - the resource (mc1)
   *  - the usage case (how the resource will be used, actions on resource)
   *  - what happens when resource is released (no longer needed)
   * */
  val mc1Bracket: IO[String] = mc1.bracket { // mc1 is the resource
    value => IO(s"value: $value").debug // actions performed on mc1, when it is in use
  } {
    value => IO(s"releasing $value").debug.void // actions performed when/after mc1 is released
  }


  // WhenA & UnlessA conditionals
  val condition: Boolean = true
  val mc1Cond1: IO[Unit] = IO.whenA(condition)(IO("Condition was true").debug.void) // If condition == true  -> then IO(action)
  val mc1Cond2: IO[Unit] = IO.unlessA(condition)(IO("Condition was false").debug.void) // If condition == false -> then IO(action)

  val mc1Cond3: IO[Unit] = IO.raiseWhen(condition)(throw new RuntimeException()) // If condition == true  -> then throw exception
  val mc1Cond4: IO[Unit] = IO.raiseUnless(condition)(throw new RuntimeException()) // If condition == false -> then throw exception


  override def run: IO[Unit] =
  //    mc1.debug.void
  //    mc4.debug.void

  //    uncancelableFunc[IO, Throwable].debug.void
  //    mc4Listener1.debug.void
  //    mc4Listener2.debug.void

  //    mc1Finaliser1.debug.void
    mc1Finaliser2.debug.void
}
