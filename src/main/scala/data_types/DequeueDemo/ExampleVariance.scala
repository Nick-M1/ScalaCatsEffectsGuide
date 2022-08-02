package data_types.DequeueDemo

import cats.{Contravariant, Functor}
import cats.implicits._
import cats.effect._
import cats.effect.std.{Dequeue, DequeueSource, DequeueSink}
import cats.effect.unsafe.implicits.global

/** VARIANCE: <p>
 *  Dequeue is split into a DequeueSource with a Functor instance and a DequeueSink with a Contravariant functor instance. <p>
 *  Can treat a Dequeue[F, A] as a DequeueSource[F, B] by mapping with A => B <br>
 *   or as a DequeueSink[F, B] by contramapping with B => A. */

object ExampleVariance extends App {


  // COVARIANT - DequeueSource
  def covariant(list: List[Int]): IO[List[Long]] = for {
    q <- Dequeue.bounded[IO, Int](10)                                             // Creates a Dequeue with bounded max capacity of 10 items
    qOfLongs: DequeueSource[IO, Long] = Functor[DequeueSource[IO, *]].map(q)(_.toLong)    // Convert/map each item in Dequeue from Int to Long

    _ <- list.traverse(q.offer _)               // offer _ == offer(_)                    // Traverse input list & offer/add each of its items to the Dequeue
    l <- List.fill(list.length)(()).traverse(_ => qOfLongs.take)                          // Takes items off qOfLongs & add these to List l
  } yield l

  covariant(List(1,4,2,3)).flatMap(IO.println(_)).unsafeRunSync()         // List(1, 4, 2, 3)


  // CONTRAVARIANT - DequeueSink
  def contravariant(list: List[Boolean]): IO[List[Int]] = for {
    q <- Dequeue.bounded[IO, Int](10)                                             // Creates a Dequeue with bounded max capacity of 10 items
    qOfBools: DequeueSink[IO, Boolean] = Contravariant[DequeueSink[IO, *]].contramap(q)(b => if b then 1 else 0)    // Convert/map each item in Dequeue from boolean to int (true -> 1, false -> 0)

    _ <- list.traverse(qOfBools.offer _)        // offer _ == offer(_)                    // Traverse input list & offer/add each of its items to the Dequeue
    l <- List.fill(list.length)(()).traverse(_ => q.take)                                 // Takes items off qOfBools & add these to List l
  } yield l

  contravariant(List(true, false)).flatMap(IO.println(_)).unsafeRunSync()   // List(1, 0)

}
