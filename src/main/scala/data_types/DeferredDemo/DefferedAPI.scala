package data_types.DeferredDemo

import cats.effect.unsafe.implicits.global
import cats.effect.{Deferred, IO, IOApp}
import cats.syntax.all.*

/** DEFERRED: <p>
 *  A purely functional synchronization primitive which represents a single value which may not yet be available. <p>
 *  When created, a Deferred is empty. It can then be completed exactly once, and never be made empty again. <p>
 *
 *  Deferred can be used in conjunction with Ref to build complex concurrent behaviour and data structures like queues and semaphores.<p>
 *  Note: The blocking (described below) is semantic only, no actual threads are blocked by the implementation.
 * */

object DefferedAPI {

  // Simplified version of Cats-Effects CountDownLatch API

  abstract class myDeferred[F[_], A] {
    /** GET method:
     *   - get on an empty Deferred will block until the Deferred is completed
     *   - get on a completed Deferred will always immediately return its content
     *   - get is cancelable and on cancelation it will unsubscribe the registered listener, an operation that's possible for as long as the Deferred value isn't complete
     * */
    def get: F[A]
    
    /** COMPLETE method:
     *   - complete(a) on an empty Deferred will set it to a, notify any and all readers currently blocked on a call to get and return true
     *   - complete(a) on a Deferred that has already been completed will not modify its content, and will result false
     * */
    def complete(a: A): F[Boolean]
  }
}

