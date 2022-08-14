package IO_Demo.part3_ManualFibers

import cats.effect.IO.{IOCont, Uncancelable}
import cats.effect.{IO, IOApp}

import scala.concurrent.duration.*
import utils_useful.extensions.*

import java.util.concurrent.TimeoutException
import scala.io.StdIn

object CancellingIOs extends IOApp.Simple {

  /** How to cancel IOs:
   *  - fib.cancel
   *  - IO.race & other higher-level APIs
   *  - manual cancellation - difficult
   * */
  val io1: IO[Int] = IO("waiting").debug >> IO.canceled >> IO(42).debug

  /** UNCANCELABLE:
   *  Examples: Online payements */
  // onCancel method
  val onlinePayment: IO[String] = (
    IO("Payment running, don't cancel...").debug >>
      IO.sleep(1.second) >>
      IO("Payment completed").debug
  ).onCancel(IO("Payment canceled :(").debug.void)


  val onlinePaymentCanceled: IO[Unit] = for {
    fib <- onlinePayment.start
    _ <- IO.sleep(500.millis) >> fib.cancel
    _ <- fib.join
  } yield ()


  // ---------------------------------------------------------------------------------------------

  /** PASSWORD AUTHENTICATION SERVICE
   *  Has 2 parts:
   *  - input password - Can be canceled, otherwise we might block the rest indefinitely
   *  - verify password - Can't be canceled once started
   *  
   *  This codes is modified to use generic types in type_classes.MonadCancelDemo.using2
   * */

  def inputPassword(): IO[String] = {
    IO("Input password").debug >>
//      IO(StdIn.readLine())                  // Get std input from user
      IO("(typing password)").debug >>
      IO.sleep(2.seconds) >>
      IO("Password entered").debug >>
      IO("hello")
  }

  def checkPassword(password: String): Boolean =        // checks password against DB
    password.equals("hello")

  def verifyPassword(password: String): IO[Boolean] = {
    IO("verifying").debug >>
      IO.sleep(2.seconds) >>
      IO(checkPassword(password)).debug
  }

  val authFlow: IO[Unit] = IO.uncancelable { poll =>
    for {
      password <- poll(inputPassword()).onCancel(IO("Authentication timed out").debug.void)     // this is cancelable
      verified <- verifyPassword(password)                                                      // this is not cancelable
      _ <- if verified then IO("Authentication successful").debug                               // this is not cancelable
           else IO("Authentication failed").debug
    } yield ()
  }

//  val authProgram: IO[Unit] = for {
//    authFib <- authFlow.start
//    _ <- IO.sleep(8.seconds) >> IO("Authentication timeout, attempting cancel...").debug >> authFib.cancel
//    _ <- authFib.join
//  } yield ()

  val authProgram: IO[Unit] = {
    authFlow.timeout(7.seconds)
      .handleErrorWith(e => IO(s"Authentication timeout, attempting cancel...       Error: $e").debug.void)
  }




  override def run: IO[Unit] = {
//    onlinePaymentCanceled.debug.void

    authProgram.void

  }

}
