package data_types.SemaphoreDemo

import cats.effect.kernel.GenConcurrent
import cats.effect.Resource

/** SEMAPHORE: <br>
 *  A semaphore has a non-negative number of permits available:
 *   - Acquiring a permit decrements the current number of permits
 *   - Releasing a permit increases the current number of permits.
 *
 *  An acquire that occurs when there are no permits available results in fiber blocking until a permit becomes available. <br>
 *  This blocking is 'semantic blocking' as the actual thread isn't being blocked <p>
 *  Examples: Shared resource, Producer-consumer channel
 *  */

object create {
  
  // Simplified Semaphore API:
  abstract class Semaphore[F[_]] {

    /** Returns the number of permits currently available. Always non-negative. */
    def available: F[Long]

    /** Acquires n permits. <p>
     *  The returned effect semantically blocks until all requested permits are available. Note that acquires are statisfied in strict FIFO order, so given s: Semaphore[F] with 2 permits available, an acquireN(3) will always be satisfied before a later call to acquireN(1). <p>
     *  This method is interruptible */
    def acquireN(n: Long): F[Unit]

    /** Acquires a single permit. Alias for acquireN(1). */
    def acquire: F[Unit] = acquireN(1)


    /** Acquires `n` permits now and returns `true`, or returns `false` immediately. Error if `n < 0`. */
    def tryAcquireN(n: Long): F[Boolean]

    /**
     * Alias for tryAcquireN(1).
     */
    def tryAcquire: F[Boolean] = tryAcquireN(1)

    /**
     * Releases `n` permits, potentially unblocking up to `n` outstanding acquires.
     */
    def releaseN(n: Long): F[Unit]

    /**
     * Releases a single permit. Alias for releaseN(1).
     */
    def release: F[Unit] = releaseN(1)

    /**
     * Returns a [[cats.effect.kernel.Resource]] that acquires a permit, holds it for the lifetime of the resource, then releases the permit.
     */
    def permit: Resource[F, Unit]

  }


  // Companion object:
  object Semaphore {
    def apply[F[_]](n: Long)(implicit F: GenConcurrent[F, _]): F[Semaphore[F]] = ???      // Returns a Semaphore with a total of 'n' number of permits
  }
}
