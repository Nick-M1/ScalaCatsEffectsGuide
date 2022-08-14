package data_types.Hotswap

import cats.effect.kernel.Ref
import cats.effect.{IO, Resource}
import cats.effect.std.Hotswap
import java.io.File

/** ROTATING LOGGER <p>
 *  Will Log to a specific Resource. <p>
 *  After a certain number of bytes have been written, will move to Logging to a new Resource (a rotation). <p>
 *
 *  Note: The whole msg must be written on the same Logging resource <br>
 *   so if the msg can't fit on the current resource -> use a new resource
 *  */

object ExampleRotatingLogger {
  
  // Abstract logging trait
  trait Logger[F[_]] {
    def log(msg: String): IO[Unit]
  }

  def file(name: String): Resource[IO, File] = ???      // Returns the file in a Resource
  def write(file: File, msg: String): IO[Unit] = ???    // Writes a msg to the file
  
  
  
  def rotating(n: Int): Resource[IO, Logger[IO]] =
    Hotswap.create[IO, File].flatMap { hs =>          // Initialise the Hotswap
      Resource.eval {
        for {
          index <- Ref[IO].of(0)                      // Initialise an index & count Ref to 0
          count <- Ref[IO].of(0)
          
          f <- hs.swap(file("0.log"))          // Open the initial log file
          logFile <- Ref[IO].of(f)                    // save this log file in a Ref

        } yield new Logger[IO] {                      // Initialise a Logger, with method log that logs a message and puts these actions in an IO
          def log(msg: String): IO[Unit] =
            count.get.flatMap { currentCount =>

              if (msg.length() < n - currentCount) {      // If rest of msg can fit on current Logging resource
                for {                                       // Write the whole msg to the resource
                  currentFile <- logFile.get
                  _ <- write(currentFile, msg)
                  _ <- count.update(_ + msg.length())       // increase count to include the current msg's length
                } yield ()

              } else {                                    // If rest of msg can't fit on current Logging resource
                for {
                  _ <- count.set(msg.length())               // Reset the log length counter
                  idx <- index.updateAndGet(_ + 1)           // Increment the counter for the log file name
                  f <- hs.swap(file(s"$idx.log"))     // Close the old log file and open the new one

                  _ <- logFile.set(f)
                  _ <- write(f, msg)                         // write the msg to the new resource
                } yield ()
              }
            }
        }
      }
    }

}
