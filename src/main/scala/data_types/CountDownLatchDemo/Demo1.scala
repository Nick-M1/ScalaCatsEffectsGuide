package data_types.CountDownLatchDemo

import cats.implicits._
import cats.effect._
import cats.effect.std.CountDownLatch
import cats.effect.unsafe.implicits.global

/** COUNT DOWN LATCH: <p>
 *  A one-shot concurrency primitive that blocks any fibers that wait on it. <p>
 *  It is initialized with a positive integer n latches and waiting fibers are semantically blocked until
 *   all n latches are released. After this, further awaits are no-ops (equivalent to IO.unit). */

object Demo1 extends App {

  // Simplified version of Cats-Effects CountDownLatch API
  trait MyCountDownLatch[F[_]] {
    def release: F[Unit]
    def await: F[Unit]
  }


  // Using Cats-effects:
  val run: IO[Unit] = for {
    c <- CountDownLatch[IO](2)
    f <- (c.await >> IO.println("Countdown latch unblocked")).start
    _ <- c.release
    _ <- IO.println("Before latch is unblocked")
    _ <- c.release
    _ <- f.join
  } yield ()

  run.unsafeRunSync()

}
