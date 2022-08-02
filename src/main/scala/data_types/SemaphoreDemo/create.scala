package data_types.SemaphoreDemo

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
    def available: F[Long]
    def acquire: F[Unit]
    def release: F[Unit]
    // ... and more
  }

}
