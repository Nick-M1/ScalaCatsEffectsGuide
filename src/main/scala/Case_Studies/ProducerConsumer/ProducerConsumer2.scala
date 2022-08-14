package Case_Studies.ProducerConsumer

import cats.effect.*
import cats.effect.std.Console
import cats.instances.list._
import cats.syntax.all.*

import scala.collection.immutable.Queue

/** PRODUCER CONSUMER PATTERN: <p>
 * ------------------------------------------------------------------------------------------- <p>
 * OPTIMISED IMPLEMENTATION: <p>
 * Instead of using Option to represent elements retrieved from a possibly empty queue, we should instead block the caller fiber somehow if queue is empty until some element can be returned. <br>
 * This will be done by creating and keeping instances of Deferred. <br>
 * A Deferred[F, A] instance can hold one single element of some type A. <p>
 * Deferred instances are created empty, and can be filled only once. <br>
 * If some fiber tries to read the element from an empty Deferred then it will be semantically blocked until some other fiber fills (completes) it. <p>
 *
 * Thus, alongside the queue of produced but not yet consumed elements, we have to keep track of the Deferred instances created when the queue was empty that are waiting for elements to be available. <br>
 * These instances will be kept in a new queue takers. We will keep both queues in a new type State
 * */


object ProducerConsumer2 extends IOApp {


  /** Both producer and consumer will access the same shared state instance, which will be carried and safely modified by an instance of Ref.
   *
   * @param queue  Queue of items
   * @param takers Queue of blocked Fibers (added to when 'queue' is empty & will be removed from as 'queue' starts to get added to from being empty) */
  case class State[F[_], A](queue: Queue[A], takers: Queue[Deferred[F, A]]) // F = IO, A = Int

  // Implements empty method - returns IDENTITY of State (empty)
  object State {
    def empty[F[_], A]: State[F, A] = State(Queue.empty, Queue.empty)
  }

  /** Consumer - removes items from queue <p>
   * Consumer shall work as follows: <br>
   * * If queue is not empty, it will extract and return its head. The new state will keep the tail of the queue, no change on takers will be needed. <br>
   * * If queue is empty it will use a new Deferred instance as a new taker, add it to the takers queue, and 'block' the caller by invoking taker.get
   *
   * @param id     Used to identify consumers in log (unique to this consumer)
   * @param stateR Ref, shared between all consumers & producers
   * */
  def consumer[F[_] : Async : Console](id: Int, stateR: Ref[F, State[F, Int]]): F[Unit] = {

    // Takes the next item from queue (dequeue) & updates State, but also blocks this consumer Fiber if queue empty.
    val take: F[Int] = // val as it doesn't take an input (only gives an output value)
      Deferred[F, Int].flatMap { taker =>
        stateR.modify {
          case State(queue, takers) if queue.nonEmpty => {
            val (i, rest) = queue.dequeue // dequeue returns the head item & the rest as a queue
            State(rest, takers) -> Async[F].pure(i) // Got element in queue, we can just return it
          }
          case State(queue, takers) =>
            State(queue, takers.enqueue(taker)) -> taker.get // No element in queue, must block caller (taker.get) until some is available
        }.flatten
      }

    for {
      i <- take // Get next item from queue
      _ <- if (i % 10000 == 0) then Console[F].println(s"Consumer $id has reached $i items") else Async[F].unit // Every 10,000 items, log
      _ <- consumer(id, stateR) // Recursive call (loop)
    } yield ()
  }

  /** Producer adding ints (1 -> inf) to the queue <p>
   * The producer will: <br>
   * * If there are waiting takers, it will take the first in the queue and offer it the newly produced element (taker.complete).<br>
   * * If no takers are present, it will just enqueue the produced element.
   *
   * @param id       Used to identify producers in log (unique to this producer)
   * @param stateR   Ref, shared between all consumers & producers
   * @param counterR Counter that increments and its value added to queue
   */
  def producer[F[_] : Sync : Console](id: Int, counterR: Ref[F, Int], stateR: Ref[F, State[F, Int]]): F[Unit] = {

    // 'Adds' an item to queue & if there are blocked consumer Fibers (in takers[]) then unblock a single consumer for this item
    def offer(i: Int): F[Unit] = // def used as this also takes input
      stateR.modify {
        case State(queue, takers) if takers.nonEmpty => {
          val (taker, rest) = takers.dequeue // Take a consumer from takers[] (queue of blocked consumer Fibers)
          State(queue, rest) -> taker.complete(i).void // and unblock this Fiber - don't need to actually add item to queue as this unblocked consumer will consume it
        }
        case State(queue, takers) =>
          State(queue.enqueue(i), takers) -> Sync[F].unit // Add item to queue
      }.flatten

    for {
      i <- counterR.getAndUpdate(_ + 1) // Increment counter
      _ <- offer(i) // 'add' this item to queue
      _ <- if (i % 10000 == 0) Console[F].println(s"Producer $id has reached $i items") else Sync[F].unit // Every 10,000 items, log
      _ <- producer(id, counterR, stateR) // Recursive call (loop)
    } yield ()
  }


  /** Run method: <p>
   * Initialises the counter, state Refs, 10 consumers & 10 producers */
  override def run(args: List[String]): IO[ExitCode] =
    for {
      stateR <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int]) // Initialise stateR & counterR
      counterR <- Ref.of[IO, Int](1)

      producers = List.range(1, 11).map(producer(_, counterR, stateR)) // 10 producers
      consumers = List.range(1, 11).map(consumer(_, stateR)) // 10 consumers

      res <- (producers ++ consumers)
        .parSequence.as(ExitCode.Success) // Run producers and consumers in parallel until done (likely by user cancelling with CTRL-C)
        .handleErrorWith { t =>
          Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
        }
    } yield res

}
