package data_types.SupervisorDemo


import cats.effect.{Fiber, Concurrent, Resource}

/** SUPERVISOR:
 *
 * A supervisor spawns fibers whose lifecycles are bound to that of the supervisor.
 *
 *  The supervisor is managed by a singular fiber to which the lifecycles of all spawned fibers are bound.
 *
 * Spawn provides multiple ways to spawn a fiber to run an action:
 *  - Spawn[F]#start: start and forget, no lifecycle management for the spawned fiber
 *  - Spawn[F]#background: ties the lifecycle of the spawned fiber to that of the fiber that invoked background
 *
 * However, to spawn a fiber that outlives the scope that created it & still want to control its lifecycle <br> -> Use Supervisor
 *
 * Note: Any fibers created via the supervisor will be finalized when the supervisor itself is finalized via Resource#use.
 * */

object create {

  // Simplified Supervisor API
  trait Supervisor[F[_]] {

    /** Starts the supplied effect fa on the supervisor. <p>
     *  Returns a Fiber that represents a handle to the started fiber. */
    def supervise[A](fa: F[A]): F[Fiber[F, Throwable, A]]
  }

  object Supervisor { // companion object for easier constructor syntax
    def apply[F[_]](implicit F: Concurrent[F]): Resource[F, Supervisor[F]] = ???
  }

}
