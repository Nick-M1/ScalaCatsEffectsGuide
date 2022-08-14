package data_types.DispatcherDemo

import cats.effect.{IO, IOApp}
import cats.effect.std.{Dispatcher, Queue}

/** QUEUE EXAMPLE: <p>
 *  Want to add each message received to a queue <p>
 *
 *  */

object ExampleQueue extends IOApp.Simple {
  
  // Interface that Dispatcher 'wraps around' - this interface creates/stores a message
  abstract class ImpureInterface {
    def onMessage(msg: String): Unit    // User implements this method themselves - this methods adds a message (String) to a queue

    def init(): Unit =
      onMessage("init")

  }
  

  override def run: IO[Unit] = {

    Dispatcher[IO].use { dispatcher =>
      for {
        queue <- Queue.unbounded[IO, String]              // Initialise queue
        impureInterface <- IO.delay {                     // Create IO that contains this impure interface
          new ImpureInterface {                           // Creates an instance of ImpureInterface & implements its methods
            override def onMessage(msg: String): Unit =
              dispatcher.unsafeRunSync(queue.offer(msg))      // implements this method to add a msg to the queue (synchronous action)
          }
        }

        _ <- IO.delay(impureInterface.init())             // Uses this object's (ImpureInterface) method
        value <- queue.tryTake                            // Take item from queue

      } yield value match {                               // Error-handling based on what 'value': Option[String] is
        case Some(v) => println(s"Value found in queue! $v")      // Successfully added to queue
        case None => println("Value not found in queue :(")       // Wasn't successfully added to queue
      }
    }
  }
}
