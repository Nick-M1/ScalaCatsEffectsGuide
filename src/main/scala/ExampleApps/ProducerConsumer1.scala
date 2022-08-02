package ExampleApps

import cats.effect._
import cats.effect.std.Console
import cats.syntax.all._
import collection.immutable.Queue

/** PRODUCER CONSUMER PATTERN: <p>
 *  1 or more producers insert data on a shared data structure like a queue while one or more consumers extract data from it. <br>
 *  Readers and writers run concurrently. <p>
 *  If the queue is empty then readers will block until data is available, if the queue is full then writers will wait for some 'bucket' to be free. <br>
 *  Only one writer at a time can add data to the queue to prevent data corruption. <br>
 *  Also only one reader can extract data from the queue so no two readers get the same data item. <p>
 *
 * Variations of this problem exist depending on whether there are more than one consumer/producer, or whether the data structure sitting between them is size-bounded or not. <br>
 * The solutions discussed here are suited for multi-consumer and multi-reader settings. <br>
 * Initially we will assume an unbounded data structure, and later present a solution for a bounded one. <p> <p>
 *
 * ------------------------------------------------------------------------------------------- <p>
 * INITIAL IMPLEMENTATION: <p>
 * Use a simple queue as the intermediate structure where producer(s) can insert data to and consumer(s) extracts data from.<p>
 * Initially there will be only one producer and one consumer.<br>
 * Producer will generate a sequence of integers (1, 2, 3...), consumer will just read that sequence.<br>
 * Our shared queue will be an instance of an immutable Queue[Int].<p>
 *
 * Accesses to the queue will be concurrent, so need to protect the queue so only one fiber at a time is handling it. <br>
 * Therefore, use Ref shared data. <br>
 * A Ref instance wraps some given data and implements methods to manipulate that data in a safe manner. <br>
 * When some fiber is running one of those methods, any other call to any method of the Ref instance will be blocked. <p>
 *
 * The Ref wrapping our queue will be Ref[F, Queue[Int]] (for some F[_]).
 * */


object ProducerConsumer1 extends IOApp {

  // Producer adding ints (1 -> inf) to the queue
  def producer[F[_]: Sync: Console](queueR: Ref[F, Queue[Int]], counter: Int): F[Unit] = for {
    _ <- if(counter % 10000 == 0) then Console[F].println(s"Produced $counter items") else Sync[F].unit   // Every 10_000 items added, print a log message
    _ <- queueR.getAndUpdate(_.enqueue(counter + 1))    // adds data to queue in immutable way (returns a new queue with the updated values). Ref used so accessing queue will be blocked for other fibers while this takes place
    _ <- producer(queueR, counter + 1)                  // Recursive call (loop), with incremented counter int
  } yield ()


  // Consumer removes items from queue
  def consumer[F[_]: Sync: Console](queueR: Ref[F, Queue[Int]]): F[Unit] = for {
      iO <- queueR.modify{ queue =>     // this gets the Queue from queueR (Ref)
        queue.dequeueOption.fold((queue, Option.empty[Int])){case (i,queue) => (queue, Option(i))}      // If queue not empty, then dequeue, else nothing ( None.fold(...) = None )
      }
      _ <- if(iO.exists(_ % 10000 == 0)) Console[F].println(s"Consumed ${iO.get} items") else Sync[F].unit // Every 10_000 items removed, print a log message
      _ <- consumer(queueR)                             // Recursive call (loop)
    } yield ()


  /** Run method - using parMapN: */
  override def run(args: List[String]): IO[ExitCode] = for {
    queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])          // Initiate queueR (Ref), with its queue being empty

    res <- (consumer(queueR), producer(queueR, 0))
      .parMapN((_, _) => ExitCode.Success)    // Run producer and consumer in parallel until done (likely by user cancelling with CTRL-C)
      .handleErrorWith { t =>                 // Error handling -> Log & exit
        Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
      }
  } yield res

  /** Run method - using start & join: <p>
   *  However not advisable to handle fibers manually. <br>
   *  If there is an error in a fiber the join call to that fiber will not raise it, it will return normally and you must
   *   explicitly check the Outcome instance returned by the .join call to see if it errored. Also, the other fibers will keep running unaware of what happened.
   */
//  def run(args: List[String]): IO[ExitCode] = for {
//    queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])      // Initiate queueR (Ref), with its queue being empty
//
//    producerFiber <- producer(queueR, 0).start      // Explicitly create new Fibers
//    consumerFiber <- consumer(queueR).start
//    _ <- producerFiber.join                                 // Runs Fibers & waits for them to finish
//    _ <- consumerFiber.join
//  } yield ExitCode.Error



  /** Cats Effect provides additional joinWith or joinWithNever methods to make sure at least that the error is raised with the usual MonadError semantics (e.g., short-circuiting).
   *  Now that we are raising the error, we also need to cancel the other running fibers.
   *  We can easily get ourselves trapped in a tangled mess of fibers to keep an eye on.
   *  On top of that the error raised by a fiber is not promoted until the call to joinWith or .joinWithNever is reached.
   *  So in our example above if consumerFiber raises an error then we have no way to observe that until the producer fiber has finished.
   *  Alarmingly, note that in our example the producer never finishes and thus the error would never be observed! And even if the producer fiber did finish, it would have been consuming resources for nothing.
   *
   * In contrast, parMapN does promote any error it finds to the caller and takes care of canceling the other running fibers.
   * As a result parMapN is simpler to use, more concise, and easier to reason about.
   * Because of that, unless you have some specific and unusual requirements you should prefer to use higher level commands such as parMapN or parSequence to work with fibers.
   * */

}
