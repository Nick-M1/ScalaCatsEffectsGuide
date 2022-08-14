package data_types.RefDemo

import cats.FlatMap
import cats.data.State
import cats.effect.Ref
import cats.effect.Ref.Make

/** REF: <p>
 *  An asynchronous, concurrent mutable reference. <p>
 *  Provides safe concurrent access and modification of its content, but no functionality for synchronisation, which is instead handled by Deferred. <p>
 *  For this reason, a Ref is always initialised to a value. <p>
 *  The default implementation is nonblocking and lightweight, consisting essentially of a purely functional wrapper over an AtomicReference. <p>
 *  Consequently it must not be used to store mutable data as AtomicReference#compareAndSet and friends are not
 *   threadsafe and are dependent upon object reference equality.
 *  */

object create {

  // Simplified Ref API:
  abstract class myRef[F[_], A] {

    /**
     * Obtains the current value.
     *
     * Since `Ref` is always guaranteed to have a value, the returned action completes immediately after being bound.
     */
    def get: F[A]

    /**
     * Sets the current value to `a`.
     *
     * The returned action completes after the reference has been successfully set.
     */
    def set(a: A): F[Unit]

    /** Updates the current value using function `f` and returns the previous value. */
    def getAndUpdate(f: A => A): F[A]

    /**
     * Replaces the current value with `a`, returning the previous value.
     */
    def getAndSet(a: A): F[A]

    /**
     * Updates the current value using `f`, and returns the updated value.
     */
    def updateAndGet(f: A => A): F[A]


    /**
     * Attempts to modify the current value once <p>
     * Returns `false` if another concurrent modification completes between the time the variable is read and the time it is set.
     */
    def tryUpdate(f: A => A): F[Boolean]

    /**
     * Like `tryUpdate` but allows the update function to return an output value of type `B`. The
     * returned action completes with `None` if the value is not updated successfully and
     * `Some(b)` otherwise.
     */
    def tryModify[B](f: A => (A, B)): F[Option[B]]

    /**
     * Modifies the current value using the supplied update function
     */
    def update(f: A => A): F[Unit]

    /**
     * Like `tryModify` but does not complete until the update has been successfully made (blocks the fiber calling it).
     */
    def modify[B](f: A => (A, B)): F[B]

    /**
     * Update the value of this ref with a state computation.
     *
     * The current value of this ref is used as the initial state and the computed output state is
     * stored in this ref after computation completes.
     */
    def tryModifyState[B](state: State[A, B]): F[Option[B]]

    /**
     * Like [[tryModifyState]] but retries (i.e. blocks the fiber calling it) the modification until successful.
     */
    def modifyState[B](state: State[A, B]): F[B]

  }

  // Companion object for Ref:
  object myRef {

    /**
     * Creates a thread-safe, concurrent mutable reference initialized to the supplied value.
     */
    def of[F[_], A](a: A)(implicit mk: Make[F]): F[Ref[F, A]] = ???

    /**
     * Creates a Ref starting with the value of the one in `source`. <p>
     * Updates of either of the Refs will not have an effect on the other (assuming A is immutable).
     */
    def copyOf[F[_] : Make : FlatMap, A](source: Ref[F, A]): F[Ref[F, A]] = ???

    /**
     * Creates a Ref starting with the result of the effect `fa`.
     */
    def ofEffect[F[_] : Make : FlatMap, A](fa: F[A]): F[Ref[F, A]] = ???


  }

}
