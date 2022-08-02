package data_types.DispatcherDemo

import cats.effect.{IO, IOApp}
import cats.effect.std.{Dispatcher, Queue}
import data_types.DispatcherDemo.Create.ImpureInterface

/** QUEUE EXAMPLE: <p>
 *  Want to add each message received to a queue <p>
 *
 *  */

object ExampleQueue extends IOApp.Simple {

  override def run: IO[Unit] = {

    Dispatcher[IO].use { dispatcher =>
      for {
        queue <- Queue.unbounded[IO, String]              // Initialse queue
        impureInterface <- IO.delay {                     // Delay main thread until actions inside have finished
          new ImpureInterface {                             // Creates ImpureInterface so that each item an item/msg is added to queue -> prints(...)
            override def onMessage(msg: String): Unit =
              dispatcher.unsafeRunSync(queue.offer(msg))
          }
        }

        _ <- IO.delay(impureInterface.init())             // Initialises this ImpureInterface in the thread
        value <- queue.tryTake                            // Attemps to take from queue

      } yield value match {
        case Some(v) => println(s"Value found in queue! $v")
        case None => println("Value not found in queue :(")
      }
    }
  }
}
