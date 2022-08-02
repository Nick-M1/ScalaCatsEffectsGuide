package ExampleApps

import cats.effect.*
import cats.effect.std.Console
import cats.instances.list.*
import cats.syntax.all.*
import cats.effect.syntax.all._

import scala.collection.immutable.Queue

/** PRODUCER CONSUMER PATTERN: <p>
 * ------------------------------------------------------------------------------------------- <p>
 * IMPLEMENTATION WITH CANCELLATION SAFETY: <p>
 * If a fiber is cancelled, it will not be able to progress, but we have set that operation inside an uncancelable region.<br>
 * So there is no way to cancel that blocked fiber!<br>
 * For example, we cannot set a timeout on its execution! Thus, if the offerer is never completed then that fiber will never finish.<p>
 *
 * Use Poll[F], which is passed as parameter by F.uncancelable.<br>
 * Poll[F] is used to define cancelable code inside the uncancelable region.<br>
 * So if the operation to run was offerer.get we will embed that call inside the Poll[F], thus ensuring the blocked fiber can be canceled<p>
 *
 * Finally, we must also take care of cleaning up the state if there is indeed a cancellation.<br>
 * That cleaning up will have to remove the offerer from the list of offerers kept in the state, as it shall never be completed.
 * */


object ProducerConsumer4 extends IOApp {


  /** Both producer and consumer will access the same shared state instance, which will be carried and safely modified by an instance of Ref.
   *  @param queue Queue of items
   *  @param capacity Max number of items that queue can store
   *  @param takers Queue of blocked consumer Fibers (added to when 'queue' is empty & will be removed from as 'queue' starts to get added to from being empty)
   *  @param offerers Queue of blocked provider Fibers (added to when 'queue' at max capacity & will be removed from as 'queue' starts empty from being empty)*/
  case class State[F[_], A](queue: Queue[A], capacity: Int, takers: Queue[Deferred[F,A]], offerers: Queue[(A, Deferred[F,Unit])])

  // Implements empty method - returns IDENTITY of State (empty)
  object State {
    def empty[F[_], A](capacity: Int): State[F, A] = State(Queue.empty, capacity, Queue.empty, Queue.empty)
  }


  // CONSUMER
  def consumer[F[_]: Async: Console](id: Int, stateR: Ref[F, State[F, Int]]): F[Unit] = {

    val take: F[Int] =
      Deferred[F, Int].flatMap { taker =>
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

    for {
      i <- take
      _ <- if(i % 10000 == 0) then Console[F].println(s"Consumer $id has reached $i items") else Async[F].unit
      _ <- consumer(id, stateR)
    } yield ()
  }


  // PRODUCER
  def producer[F[_]: Async: Console](id: Int, counterR: Ref[F, Int], stateR: Ref[F, State[F,Int]]): F[Unit] = {

    def offer(i: Int): F[Unit] =
      Deferred[F, Unit].flatMap[Unit] { offerer =>
        Async[F].uncancelable { poll => // `poll` used to embed cancelable code, i.e. the call to `offerer.get`
          stateR.modify {
            case State(queue, capacity, takers, offerers) if takers.nonEmpty =>
              val (taker, rest) = takers.dequeue
              State(queue, capacity, rest, offerers) -> taker.complete(i).void
            case State(queue, capacity, takers, offerers) if queue.size < capacity =>
              State(queue.enqueue(i), capacity, takers, offerers) -> Async[F].unit
            case State(queue, capacity, takers, offerers) =>
              val cleanup = stateR.update { s => s.copy(offerers = s.offerers.filter(_._2 ne offerer)) }
              State(queue, capacity, takers, offerers.enqueue(i -> offerer)) -> poll(offerer.get).onCancel(cleanup)
          }.flatten
        }
      }

    for {
      i <- counterR.getAndUpdate(_ + 1)
      _ <- offer(i)
      _ <- if (i % 10000 == 0) then Console[F].println(s"Producer $id has reached $i items") else Async[F].unit
      _ <- producer(id, counterR, stateR)
    } yield ()
  }


  /** Run method: <p>
   *  Initialises the counter, state Refs, 10 consumers & 10 producers and sets queue max capacity to 100 items */
  override def run(args: List[String]): IO[ExitCode] = for {
    stateR <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int](capacity = 100))
    counterR <- Ref.of[IO, Int](1)
    producers = List.range(1, 11).map(producer(_, counterR, stateR)) // 10 producers
    consumers = List.range(1, 11).map(consumer(_, stateR))           // 10 consumers

    res <- (producers ++ consumers)
      .parSequence.as(ExitCode.Success) // Run producers and consumers in parallel until done (likely by user cancelling with CTRL-C)
      .handleErrorWith { t =>
        Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
      }
  } yield res

}
