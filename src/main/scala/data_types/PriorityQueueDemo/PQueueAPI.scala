package data_types.PriorityQueueDemo

import cats.Order
import cats.implicits._
import cats.effect._
import cats.effect.std.PQueue
import cats.effect.unsafe.implicits.global

/** PRIORITY QUEUE: <p>
 *  A purely-functional concurrent implementation of a priority queue (based on a binomial heap), using a cats Order instance to determine the relative priority of elements. <p>
 *  
 *  Like a Queue, but elements with the highest priority will be removed 1st.
 *  When using ints, the higher the value == higher priority)
 * */

object PQueueAPI extends App {

  // Simplified Priority Queue API:
  trait myPQueue[F[_], A : Order] {

    // Taking:

    /** Dequeues the least element from the PQueue, possibly fiber blocking until an element becomes available. <p>
     *  O(log(n))
     *  Note: If there are multiple elements with least priority, the order in which they are dequeued is undefined. <br>
     *  Use an additional Ref[F, Long] to track insertion, and embed that information into your instance for Order[A].*/
    def take: F[A]

    /** Attempts to dequeue the least element from the PQueue, if one is available without fiber blocking. <p>
     *  O(log(n))
     *  Returns an Option, where None is returned if no element was available to take
     *  */
    def tryTake: F[Option[A]]


    // Offering:

    /** Enqueues the given element, possibly fiber blocking until sufficient capacity becomes available. <p>
     *  O(log(n)) */
    def offer(a: A): F[Unit]

    /** Attempts to enqueue the given element without fiber blocking. <p>
     *  O(log(n))
     *  Returns true or false depending on if the 'try' succeeded */
    def tryOffer(a: A): F[Boolean]
  }

  /* Also has a companion object with methods bounded(capacity: Int) and unbounded(), which return a PQueue with a max capacity of 'capacity' and with no max capacity respectively */
  
}
