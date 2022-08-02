package data_types.QueueDemo

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
 *  * circularBuffer(capacity: Int): offer never blocks but the oldest elements are discarded in favour of new elements when the queue is full
 *  */

object create {

  // Simplified Queue API
  trait myQueue[F[_], A] {
    def take: F[A]
    def tryTake: F[Option[A]]

    def offer(a: A): F[Unit]
    def tryOffer(a: A): F[Boolean]
  }

}
