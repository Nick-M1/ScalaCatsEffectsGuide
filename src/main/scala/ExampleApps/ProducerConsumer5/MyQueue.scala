package ExampleApps.ProducerConsumer5

import cats.effect.{Async, Deferred, Ref}
import cats.syntax.all._
import cats.effect.syntax.all._

import scala.collection.immutable.{Queue => ScQueue}    // scala queue alias as ScQueue

/** PRODUCER CONSUMER PATTERN: <p>
 * ------------------------------------------------------------------------------------------- <p>
 * SPLITTING OFF FUNCTIONALITY: <p>
 * Can split off take & offer methods contained in consumer & provider to be methods of a Queue
 * So, we are able to use this Queue for other applications in the future
 * */


private final case class State[F[_], A](queue: ScQueue[A], capacity: Int, takers: ScQueue[Deferred[F,A]], offerers: ScQueue[(A, Deferred[F,Unit])])

private object State {
  def empty[F[_], A](capacity: Int): State[F, A] = State(ScQueue.empty[A], capacity, ScQueue.empty[Deferred[F,A]], ScQueue.empty[(A, Deferred[F, Unit])])
}


class Queue[F[_]: Async, A](stateR: Ref[F, State[F, A]]) {

  // customised dequeue method
  val take: F[A] =
    Deferred[F, A].flatMap { taker =>
      Async[F].uncancelable { poll =>
        stateR.modify {
          case State(queue, capacity, takers, offerers) if queue.nonEmpty && offerers.isEmpty =>
            val (i, rest) = queue.dequeue
            State(rest, capacity, takers, offerers) -> Async[F].pure(i)
          case State(queue, capacity, takers, offerers) if queue.nonEmpty =>
            val (i, rest) = queue.dequeue
            val ((move, release), tail) = offerers.dequeue
            State(rest.enqueue(move), capacity, takers, tail) -> release.complete(()).as(i)
          case State(queue, capacity, takers, offerers) if offerers.nonEmpty =>
            val ((i, release), rest) = offerers.dequeue
            State(queue, capacity, takers, rest) -> release.complete(()).as(i)
          case State(queue, capacity, takers, offerers) =>
            val cleanup = stateR.update { s => s.copy(takers = s.takers.filter(_ ne taker)) }
            State(queue, capacity, takers.enqueue(taker), offerers) -> poll(taker.get).onCancel(cleanup)
        }.flatten
      }
    }

  // customised enqueue method
  def offer(a: A): F[Unit] =
    Deferred[F, Unit].flatMap[Unit]{ offerer =>
      Async[F].uncancelable { poll =>
        stateR.modify {
          case State(queue, capacity, takers, offerers) if takers.nonEmpty =>
            val (taker, rest) = takers.dequeue
            State(queue, capacity, rest, offerers) -> taker.complete(a).void
          case State(queue, capacity, takers, offerers) if queue.size < capacity =>
            State(queue.enqueue(a), capacity, takers, offerers) -> Async[F].unit
          case State(queue, capacity, takers, offerers) =>
            val cleanup = stateR.update { s => s.copy(offerers = s.offerers.filter(_._2 ne offerer)) }
            State(queue, capacity, takers, offerers.enqueue(a -> offerer)) -> poll(offerer.get).onCancel(cleanup)
        }.flatten
      }
    }

}

// Companion object to make a new Queue
object Queue {
  def apply[F[_]: Async, A](capacity: Int): F[Queue[F, A]] =
    Ref.of[F, State[F, A]](State.empty[F, A](capacity)).map(st => new Queue(st))
}
