package data_types.Hotswap

import cats.effect.Resource

/** HOTSWAP: <p>
 *  Constructing a new Resource inside the body of a Resource#use can lead to memory leaks as the outer resource is not
 *    finalized until after the inner resource is released. <br>
 *  Consider for example writing a logger that will rotate log files every n bytes. <p>
 *
 *  Hotswap addresses this by exposing a linear sequence of resources as a single Resource. <br>
 *  We can run the finalizers for the current resource and advance to the next one in the sequence using Hotswap#swap. <br>
 *  An error may be raised if the previous resource in the sequence is referenced after swap is invoked (as the resource will have been finalized).
 *  */

object create {

  // Simplified Hotswap API
  sealed trait Hotswap[F[_], R] {
    def swap(next: Resource[F, R]): F[R]
  }

}
