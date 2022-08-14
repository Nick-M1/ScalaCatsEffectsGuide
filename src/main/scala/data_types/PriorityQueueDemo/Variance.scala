package data_types.PriorityQueueDemo

import cats.Order
import cats.{Contravariant, Functor}
import cats.implicits._
import cats.effect._
import cats.effect.std.{PQueue, PQueueSource, PQueueSink}
import cats.effect.unsafe.implicits.global

/** VARIANCE: <p>
 *  PQueue is split into a PQueueSource with a Functor instance and a PQueueSink with a Contravariant functor instance. <p>
 *  This allows us to treat a PQueue[F, A] as a PQueueSource[F, B] by mapping with A => B <br>
 *   or as a PQueueSink[F, B] by contramapping with B => A.*/

object Variance extends App {

  implicit val orderForInt: Order[Int] = Order.fromLessThan((x, y) => x < y)      // How to order 2 ints

  // COVARIANT
  def covariant(list: List[Int]): IO[List[Long]] = for {
    pq <- PQueue.bounded[IO, Int](10)
    pqOfLongs: PQueueSource[IO, Long] = Functor[PQueueSource[IO, *]].map(pq)(_.toLong)
    _ <- list.traverse(pq.offer _)
    l <- List.fill(list.length)(()).traverse(_ => pqOfLongs.take)
  } yield l

  covariant(List(1,4,2,3)).flatMap(IO.println(_)).unsafeRunSync()             // List(1, 2, 3, 4)


  // CONTRAVARIANT
  def contravariant(list: List[Boolean]): IO[List[Int]] = for {
    pq <- PQueue.bounded[IO, Int](10)
    pqOfBools: PQueueSink[IO, Boolean] =
      Contravariant[PQueueSink[IO, *]].contramap(pq)(b => if (b) 1 else 0)
    _ <- list.traverse(pqOfBools.offer _)
    l <- List.fill(list.length)(()).traverse(_ => pq.take)
  } yield l

  contravariant(List(true, false)).flatMap(IO.println(_)).unsafeRunSync()     // List(0, 1)

}
