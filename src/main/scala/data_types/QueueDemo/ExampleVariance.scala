package data_types.QueueDemo

import cats.{Contravariant, Functor}
import cats.implicits._
import cats.effect._
import cats.effect.std.{Queue, QueueSource, QueueSink}
import cats.effect.unsafe.implicits.global

/** VARIANCE: <p>
 *  Queue is split into a QueueSource with a Functor instance and a QueueSink with a Contravariant functor instance. <p>
 *  This allows us to treat a Queue[F, A] as a QueueSource[F, B] by mapping with A => B <br>
 *   or as a QueueSink[F, B] by contramapping with B => A. */

object ExampleVariance {

  def covariant(list: List[Int]): IO[List[Long]] = for {
    q <- Queue.bounded[IO, Int](10)
    qOfLongs: QueueSource[IO, Long] = Functor[QueueSource[IO, *]].map(q)(_.toLong)
    
    _ <- list.traverse(q.offer _)
    l <- List.fill(list.length)(()).traverse(_ => qOfLongs.take)
  } yield l

  covariant(List(1,4,2,3)).flatMap(IO.println(_)).unsafeRunSync()

  
  def contravariant(list: List[Boolean]): IO[List[Int]] = for {
    q <- Queue.bounded[IO, Int](10)
    qOfBools: QueueSink[IO, Boolean] =
      Contravariant[QueueSink[IO, *]].contramap(q)(b => if (b) 1 else 0)
      
    _ <- list.traverse(qOfBools.offer _)
    l <- List.fill(list.length)(()).traverse(_ => q.take)
  } yield l

  contravariant(List(true, false)).flatMap(IO.println(_)).unsafeRunSync()
  
  // ANNOTATE FROM DEQUEUE

}
