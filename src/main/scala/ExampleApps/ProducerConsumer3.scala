package ExampleApps

import cats.effect.*
import cats.effect.std.Console
import cats.instances.list.*
import cats.syntax.all.*

import scala.collection.immutable.Queue

/** PRODUCER CONSUMER PATTERN: <p>
 * ------------------------------------------------------------------------------------------- <p>
 * IMPLEMENTATION WITH BOUNDED QUEUE: <p>
 * The queue will now have a maximum size.<br>
 * When the queue becomes full, producers will wait (be blocked) until the queue had space <p>
 *
 * Need to keep of track of waiting producers in a new queue 'offerers', that will be added to State (with 'takers') <br>
 * For each waiting producer the offerers queue will keep a Deferred[F, Unit] that will be used to block the producer until
 * the element it offers can be added to queue or directly passed to some consumer (taker). <p>
 *
 * Alongside the Deferred instance we need to keep as well the actual element offered by the producer in the offerers queue.
 * */


object ProducerConsumer3 extends IOApp {


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


  /** Consumer - removes items from queue <p><br>
   *  If queue is not empty: <p>
   *   * If offerers is empty then it will extract and return queue's head. <p>
   *   * If offerers is not empty (there is some producer waiting), then queue head gets returned to consumer.
   *       Now, there is a free space in item queue, so remove a producer form offerers[] & unblock this producer so it can add to queue. <p><p>
   *
   *  If queue is empty: <p>
   *   * If offerers is empty then there is nothing we can give to the caller, so a new taker is created and added to takers while caller is blocked with taker.get. <br>
   *   * If offerers is not empty then the first offerer in queue is extracted, its Deferred instance released while the offered element is returned to the caller. <p>
   *
   *  @param id Used to identify consumers in log (unique to this consumer)
   *  @param stateR Ref, shared between all consumers & producers
   * */
  def consumer[F[_]: Async: Console](id: Int, stateR: Ref[F, State[F, Int]]): F[Unit] = {

    // Takes the next item from queue (dequeue) & updates State.
    val take: F[Int] =
      Deferred[F, Int].flatMap { taker =>
        stateR.modify {
          case State(queue, capacity, takers, offerers) if queue.nonEmpty && offerers.isEmpty =>    // if queue not empty & no blocked producers
            val (i, rest) = queue.dequeue                                                               // this consumer takes next item on queue
            State(rest, capacity, takers, offerers) -> Async[F].pure(i)
          case State(queue, capacity, takers, offerers) if queue.nonEmpty =>                        // if queue not empty & there are blocked producers
            val (i, rest) = queue.dequeue                                                               // take a producer off offerers & unblock it, then give this consumer the next item from this unblocked producer
            val ((move, release), tail) = offerers.dequeue
            State(rest.enqueue(move), capacity, takers, tail) -> release.complete(()).as(i)
          case State(queue, capacity, takers, offerers) if offerers.nonEmpty =>                     // if queue is empty & there are blocked producers
            val ((i, release), rest) = offerers.dequeue                                                 // take a producer off offerers & unblock it
            State(queue, capacity, takers, rest) -> release.complete(()).as(i)
          case State(queue, capacity, takers, offerers) =>                                          // if queue is empty & no blocked producers
            State(queue, capacity, takers.enqueue(taker), offerers) -> taker.get                        // block this consumer & add to takers[]
        }.flatten
      }

    for {
      i <- take
      _ <- if(i % 10000 == 0) Console[F].println(s"Consumer $id has reached $i items") else Async[F].unit
      _ <- consumer(id, stateR)
    } yield ()
  }

  /** Producer adding ints (1 -> inf) to the queue <p>
   *  The producer will: <br>
   *  * If there are waiting takers, it will take the first in the queue and offer it the newly produced element (taker.complete).<br>
   *  * If no takers are present, it will just enqueue the produced element.
   *
   * @param id Used to identify producers in log (unique to this producer)
   * @param stateR Ref, shared between all consumers & producers
   * @param counterR Counter that increments and its value added to queue
   */
  def producer[F[_]: Async: Console](id: Int, counterR: Ref[F, Int], stateR: Ref[F, State[F,Int]]): F[Unit] = {

    // 'Adds' an item to queue & if there are blocked consumer Fibers (in takers[]) then unblock a single consumer for this item
    def offer(i: Int): F[Unit] =
      Deferred[F, Unit].flatMap[Unit]{ offerer =>
        stateR.modify {
          case State(queue, capacity, takers, offerers) if takers.nonEmpty =>             // if there are blocked consumers
            val (taker, rest) = takers.dequeue                                                // unblock a consumer from tackers & give this consumer this item from the producer
            State(queue, capacity, rest, offerers) -> taker.complete(i).void
          case State(queue, capacity, takers, offerers) if queue.size < capacity =>       // if no blocked consumers & queue below max capacity
            State(queue.enqueue(i), capacity, takers, offerers) -> Async[F].unit              // this producer adds an item to queue
          case State(queue, capacity, takers, offerers) =>                                // if no blocked consumers & queue at max capacity
            State(queue, capacity, takers, offerers.enqueue(i -> offerer)) -> offerer.get     // block this producer & enqueue it to offerers
        }.flatten
      }

    for {
      i <- counterR.getAndUpdate(_ + 1)
      _ <- offer(i)
      _ <- if(i % 10000 == 0) Console[F].println(s"Producer $id has reached $i items") else Async[F].unit
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
