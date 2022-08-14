package data_types.Hotswap

import cats.effect.Resource

/** HOTSWAP: <p>
 *  Constructing a new Resource inside the body of a Resource#use can lead to memory leaks as the outer resource is not
 *    finalized until after the inner resource is released. <br>
 *  Consider for example writing a logger that will rotate log files every n bytes. <p>
 *
 *  Hotswap addresses this by exposing a linear sequence of resources as a single Resource. <br>
 *  We can run the finalizers for the current resource and advance to the next one in the sequence using Hotswap#swap. <br>
 *  An error may be raised if the previous resource in the sequence is referenced after swap is invoked (as the resource will have been finalized). <p>
 *  
 *  The following diagram illustrates the linear allocation and release of three resources `r1`, `r2`, and `r3` cycled through Hotswap:
 *
 * {{{
 * >----- swap(r1) ---- swap(r2) ---- swap(r3) ----X
 * |        |             |             |          |
 * Creation |             |             |          |
 *         r1 acquired    |             |          |
 *                       r2 acquired    |          |
 *                       r1 released   r3 acquired |
 *                                     r2 released |
 *                                                 r3 released
 * }}}
 *  
 *  
 *  */

object HotswapApi {

  // Simplified Hotswap API
  sealed trait Hotswap[F[_], R] {
    
    /** Allocates a new resource, closes the previous one if it exists, and returns the newly allocated R. <p>
     *  When the lifetime of the Hotswap is completed, the resource allocated by the most recent swap will be finalized. */
    def swap(next: Resource[F, R]): F[R]

    /** Pops and runs the finalizer of the current resource, if it exists. */
    def clear: F[Unit]
  }

}
