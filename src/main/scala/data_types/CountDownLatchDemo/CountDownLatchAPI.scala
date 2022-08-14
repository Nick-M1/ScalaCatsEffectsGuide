package data_types.CountDownLatchDemo


/** COUNT-DOWN-LATCH: <p> 
 * Concurrency abstraction that supports fiber blocking until n latches are released. <p>
 * Note that this has 'one-shot' semantics - once the counter reaches 0 then release and await will forever be no-ops */
object CountDownLatchAPI {

  // Simplified version of Cats-Effects CountDownLatch API
  trait MyCountDownLatch[F[_]] {

    /** Release a latch, decrementing the remaining count and releasing any fibers that are blocked if the count reaches 0 */
    def release: F[Unit]

    /** Semantically block until the count reaches 0 */
    def await: F[Unit]
  }
  
  
  // Also has a companion object with method apply() -> accepts an Integer for number of latches at start
  
}
