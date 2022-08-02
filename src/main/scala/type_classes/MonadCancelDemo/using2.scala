package type_classes.MonadCancelDemo

import cats.effect.IO.{IOCont, Uncancelable}
import cats.effect.{IO, IOApp, MonadCancel}
import scala.concurrent.duration.*
import java.util.concurrent.TimeoutException
import cats.Functor
import cats.implicits.*
import cats.syntax.all._
import cats.effect.syntax.all.*

/** Generalise the code for the Password Authentication System in start.basics.part3.CancellingIOs */

object using2 extends IOApp.Simple {

  // Generic sleep thread/fiber function
  def unsafeSleep[F[_], E](duration: FiniteDuration)(using mc: MonadCancel[F, E]): F[Unit] =
    mc.pure(Thread.sleep(duration.toMillis))        // bad as Thread.sleep is thread blocking

  // Generic version of our custom debug
  extension [F[_], A](fa: F[A]) {
    def debug(using functor: Functor[F]): F[A] = fa.map { a =>
      val t = Thread.currentThread().getName
      println(s"[$t] $a")
      a
    }
  }


  // Generalised code:
  def inputPassword[F[_], E](using mc: MonadCancel[F, E]): F[String] = for {      // F is a higher-kinded type, E is an error type
    _ <- mc.pure("Input password").debug
    _ <- mc.pure("(typing password)").debug
    _ <- unsafeSleep[F, E](5.seconds)
    password <- mc.pure("hello")
  } yield password

  def checkPassword(password: String): Boolean =        // checks password against DB
    password.equals("hello")

  def verifyPassword[F[_], E](password: String)(using mc: MonadCancel[F, E]): F[Boolean] = for {
    _ <- mc.pure("verifying").debug
    _ <- unsafeSleep[F, E](2.seconds)
    check <- mc.pure(checkPassword(password)).debug
  } yield check

  def authFlow[F[_], E](using mc: MonadCancel[F, E]): F[Unit] = mc.uncancelable { poll =>
    for {
      password <- poll(inputPassword).onCancel(mc.pure("Authentication timed out, try again").debug.void)     // this is cancelable
      verified <- verifyPassword(password)                                                      // this is not cancelable
      _ <- if verified then mc.pure("Authentication successful").debug                               // this is not cancelable
           else mc.pure("Authentication failed").debug
    } yield ()
  }

    val authProgram: IO[Unit] = for {
      authFib <- authFlow[IO, Throwable].start
      _ <- IO.sleep(3.seconds) >> IO("Authentication timeout, attempting cancel...").debug >> authFib.cancel
      _ <- authFib.join
    } yield ()

//  val authProgram: IO[Unit] = {
//    authFlow[IO, Throwable].timeout(2.seconds)
//      .handleErrorWith(e => IO(s"Authentication timeout, attempting cancel...       Error: $e").debug.void)
//  }




  override def run: IO[Unit] = {
    //    onlinePaymentCanceled.debug.void

    authProgram.void

  }



}
