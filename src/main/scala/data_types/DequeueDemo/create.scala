package data_types.DequeueDemo

import cats.effect.std.Queue

/** DEQUEUE: <p>
 *  A purely-functional concurrent implementation of a double-ended queue. <p>
 *  A Dequeue may be constructed as bounded or unbounded: <br>
 *   If bounded -> offer may semantically block if the queue is already full. <p>
 *  Take is fiber blocking if the pqueue is empty.
 *  */

object create {

  // Simplified Dequeue API:
  trait Dequeue[F[_], A] extends Queue[F, A] {
    
    // To back of queue
    def offerBack(a: A): F[Unit]        // Enqueues the given element at the back of the dequeue, possibly fiber blocking until sufficient capacity becomes available.
    def tryOfferBack(a: A): F[Boolean]  // Attempts to enqueue the given element at the back of the dequeue without semantically blocking
    def takeBack: F[A]                  // Dequeues an element from the back of the dequeue, possibly fiber blocking until an element becomes available.
    def tryTakeBack: F[Option[A]]       // Attempts to dequeue an element from the back of the dequeue, if one is available without fiber blocking.
    
    // To front of queue
    def offerFront(a: A): F[Unit]
    def tryOfferFront(a: A): F[Boolean]
    def takeFront: F[A]
    def tryTakeFront: F[Option[A]]
    
    def reverse: F[Unit]                // reverses the queue, in constant time
    
    /* By default: enqueue to back of queue & dequeue from front of queue (FIFO) */
    override def offer(a: A): F[Unit] = offerBack(a)
    override def tryOffer(a: A): F[Boolean] = tryOfferBack(a)
    override def take: F[A] = takeFront
    override def tryTake: F[Option[A]] = tryTakeFront

  }
  
  
  // Also has a companion object with methods bounded(capacity: Int) and unbounded(), which return a Dequeue with a max capacity of 'capacity' and with no max capacity respectively

}
