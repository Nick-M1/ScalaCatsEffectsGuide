package IO_Demo.part4_Examples

import cats.effect.{IO, IOApp}
import scala.concurrent.duration.*

object Fizbuz extends IOApp.Simple {
  val run: IO[Unit] = for {
    ctr <- IO.ref(0)

    poll = IO.sleep(1.second) *> ctr.get

    _ <- poll.flatMap(IO.println(_)).foreverM.start
    _ <- poll.map(_ % 3 == 0).ifM(IO.println("fizz"), IO.unit).foreverM.start
    _ <- poll.map(_ % 5 == 0).ifM(IO.println("buzz"), IO.unit).foreverM.start

    _ <- (IO.sleep(1.second) *> ctr.update(_ + 1)).foreverM.void
  } yield ()
}
