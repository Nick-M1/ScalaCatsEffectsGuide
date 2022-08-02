package start

import cats.effect.{IO, IOApp}
import scala.concurrent.duration.*

/*  FIZBUZ
 *  Runs four concurrent lightweight threads, or fibers, one of which counts up an Int value once per second,
 *  while the others poll that value for changes and print in response
 */


object ExampleProgramme extends IOApp.Simple {
  val run: IO[Unit] = for {
    ctr <- IO.ref(0)
    
    poll = IO.sleep(1.second) *> ctr.get

    _ <- poll.flatMap(IO.println(_)).foreverM.start
    _ <- poll.map(_ % 3 == 0).ifM(IO.println("fizz"), IO.unit).foreverM.start
    _ <- poll.map(_ % 5 == 0).ifM(IO.println("buzz"), IO.unit).foreverM.start

    _ <- (IO.sleep(1.second) *> ctr.update(_ + 1)).foreverM.void
  } yield ()
}