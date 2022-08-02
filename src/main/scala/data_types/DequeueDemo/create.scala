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
    def offerBack(a: A): F[Unit]
    def tryOfferBack(a: A): F[Boolean]
    def takeBack: F[A]
    def tryTakeBack: F[Option[A]]
    
    // To front of queue
    def offerFront(a: A): F[Unit]
    def tryOfferFront(a: A): F[Boolean]
    def takeFront: F[A]
    def tryTakeFront: F[Option[A]]
    
    def reverse: F[Unit]
    
    // By default: add to back of queue & remove from front of queue (FIFO)
    override def offer(a: A): F[Unit] = offerBack(a)
    override def tryOffer(a: A): F[Boolean] = tryOfferBack(a)
    override def take: F[A] = takeFront
    override def tryTake: F[Option[A]] = tryTakeFront

  }

}
