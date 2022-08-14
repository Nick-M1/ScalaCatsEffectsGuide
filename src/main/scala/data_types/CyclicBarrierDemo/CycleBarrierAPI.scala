package data_types.CyclicBarrierDemo

import cats.~>

/** CYCLIC BARRIER: <p>
 *  A re-usable synchronization primitive that allows a set of fibers to wait until they've all reached the same point. <p>
 *
 *  A cyclic barrier is initialized with a positive integer n and fibers which call await are semantically blocked until n of them have invoked await,
 *   at which point all of them are unblocked and the cyclic barrier is reset. <p>
 *
 *  await cancelation is supported, in which case the number of fibers required to unblock the cyclic barrier is incremented again.
 *  */

object CycleBarrierAPI {

  // Simplified version of Cats-Effects CountDownLatch API
  trait myCyclicBarrier[F[_]] {
    
    /** Possibly semantically block until the cyclic barrier is full */
    def await: F[Unit]

    /** Modifies the context in which this cyclic barrier is executed using the natural transformation f. <p>
     *  Returns a cyclic barrier in the new context obtained by mapping the current one using the natural transformation f */
    def mapK[G[_]](f: F ~> G): myCyclicBarrier[G]
  }

}
