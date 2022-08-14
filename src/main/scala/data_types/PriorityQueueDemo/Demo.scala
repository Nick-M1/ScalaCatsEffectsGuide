package data_types.PriorityQueueDemo

import cats.Order
import cats.implicits._
import cats.effect._
import cats.effect.std.PQueue
import cats.effect.unsafe.implicits.global

/** Example of using PQueues */
object Demo extends App {
  
  // Implicity ordering - so compiler knows how to sort 2 ints
  implicit val orderForInt: Order[Int] = Order.fromLessThan((x, y) => x < y)

  def mySort(list: List[Int]): IO[List[Int]] = for {
    pq <- PQueue.bounded[IO, Int](10)                           // Initialises a bounded PQueue with max capacity 10
    _ <- list.traverse(pq.offer(_))                             // For each item in input list, adds/offers item to PQueue
    l <- List.fill(list.length)(()).traverse( _ => pq.take)     // Take each item off PQueue in its order & add to list
  } yield l

  val list: List[Int] = List(1,4,3,7,5,2,6,9,8)
  mySort(list).flatMap(IO.println(_)).unsafeRunSync()           // res = List(1, 2, 3, 4, 5, 6, 7, 8, 9)

}
