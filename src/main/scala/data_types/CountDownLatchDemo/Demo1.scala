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


  // Using Cats-effects:
  val run: IO[Unit] = for {
    c <- CountDownLatch[IO](2)                          // Initialise CountDownLatch & set number of latches to 2
    f <- (c.await >> IO.println("Countdown latch unblocked")).start   // c.await -> on this fiber, waits (blocks) until the CountDownLatch reaches 0, then run the rest of the fiber
    _ <- c.release                                      // On the main thread, 'release' a fiber & decrement the CountDownLatch's count (now at 1)
    _ <- IO.println("Before latch is unblocked")
    _ <- c.release                                      // On the main thread, 'release' a fiber & decrement the CountDownLatch's count (now at 0, so the previous fiber that was waiting for the count to go to 0 now runs)
    _ <- f.join                                         // Wait for the completion of this secondary fiber
  } yield ()

  run.unsafeRunSync()

}
