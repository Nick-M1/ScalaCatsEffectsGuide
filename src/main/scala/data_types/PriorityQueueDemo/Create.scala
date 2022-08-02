package data_types.PriorityQueueDemo

import cats.Order
import cats.implicits._
import cats.effect._
import cats.effect.std.PQueue
import cats.effect.unsafe.implicits.global

/** PRIORITY QUEUE: <p>
 *  A purely-functional concurrent implementation of a priority queue, using a cats Order instance to determine the relative priority of elements. <p>
 *  
 *  Like a Queue, but elements with the highest priority will be removed 1st.
 *  When using ints, the higher the value == higher priority)
 * */

object Create extends App {

  // Simplified Priority Queue API:
  trait myPQueue[F[_], A : Order] {
    def take: F[A]
    def tryTake: F[Option[A]]
    def offer(a: A): F[Unit]
    def tryOffer(a: A): F[Boolean]
  }


  // Example of using PQueues:
  implicit val orderForInt: Order[Int] = Order.fromLessThan((x, y) => x < y)      // So compiler knows how to sort 2 ints

  def mySort(list: List[Int]): IO[List[Int]] = for {
    pq <- PQueue.bounded[IO, Int](10)                   // Initialises a bounded PQueue with max capacity 10
    _ <- list.traverse(pq.offer(_))                             // For each item in input list, adds/offers item to PQueue
    l <- List.fill(list.length)(()).traverse( _ => pq.take)     // Take each item off PQueue in its order & add to list
  } yield l

  val list: List[Int] = List(1,4,3,7,5,2,6,9,8)
  mySort(list).flatMap(IO.println(_)).unsafeRunSync()           // res = List(1, 2, 3, 4, 5, 6, 7, 8, 9)

}
