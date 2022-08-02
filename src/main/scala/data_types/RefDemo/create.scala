package data_types.RefDemo

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
    def get: F[A]
    def set(a: A): F[Unit]
    def modify[B](f: A => (A, B)): F[B]
    // ... and more
  }

}
