package data_types.QueueDemo

import cats.effect.GenConcurrent

/** QUEUE: <p>
 *  A purely-functional concurrent implementation of a queue. <p>
 *  
 *  take is fiber blocking when the queue is empty. <p>
 *  
 *  A Queue is constructed with different policies for the behaviour of offer when the queue has reached capacity: <br>
 *  * bounded(capacity: Int): offer is fiber blocking when the queue is full <br>
 *  * synchronous: equivalent to bounded(0) - offer and take are both blocking until another fiber invokes the opposite action <br>
 *  * unbounded: offer never blocks <br>
 *  * dropping(capacity: Int): offer never blocks but new elements are discarded if the queue is full <br>
 *  * circularBuffer(capacity: Int): offer never blocks but the oldest elements are discarded in favour of new elements when the queue is full <p>
 *
 *  The Queue stores generic F data types that are [[cats.effect.kernel.GenConcurrent]]
 *  */

object QueueAPI {

  // Simplified Queue API
  trait myQueue[F[_], A] {
    def take: F[A]
    def tryTake: F[Option[A]]

    def offer(a: A): F[Unit]
    def tryOffer(a: A): F[Boolean]
  }


  // Companion object for myQueue
  object myQueue {

    /**
     * Constructs an empty, bounded queue holding up to `capacity` elements for `F` data types <p>
     * When the queue is full (contains exactly `capacity` elements), every next Queue.offer will block the fiber calling it until an element becomes available in the Queue
     */
    def bounded[F[_], A](capacity: Int)(implicit F: GenConcurrent[F, _]): F[myQueue[F, A]] = ???

    /** Constructs a queue with the capcity of a single element */
    def synchronous[F[_], A](implicit F: GenConcurrent[F, _]): F[myQueue[F, A]] =
      bounded(0)

    /**
     * Constructs an empty, unbounded queue (no max capacity). <p>
     * Queue.offer never blocks semantically, as there is always spare capacity in the queue.
     */
    def unbounded[F[_], A](implicit F: GenConcurrent[F, _]): F[myQueue[F, A]] =
      bounded(Int.MaxValue)

    /**
     * Constructs an empty, bounded, dropping queue holding up to `capacity` elements. <p>
     * When the queue is full (contains exactly `capacity` elements), every next Queue.offer will be ignored, i.e. no other elements can be enqueued until there is sufficient capacity in the queue, and the offer effect itself will not semantically block.
     */
    def dropping[F[_], A](capacity: Int)(implicit F: GenConcurrent[F, _]): F[myQueue[F, A]] = ???

    /**
     * Constructs an empty, bounded, circular buffer queue holding up to `capacity` elements. <p>
     * The queue always keeps at most `capacity` number of elements, with the oldest element in the queue always being dropped in favor of a new elements arriving in the queue, <br>
     * and the offer effect itself will not semantically block.
     */
    def circularBuffer[F[_], A](capacity: Int)(implicit F: GenConcurrent[F, _]): F[myQueue[F, A]] = ???
  }
}
