package start.basics.part2

import cats.effect.{ExitCode, Fiber, FiberIO, IO, IOApp}
import concurrent.duration._
import utils_useful.extensions.*

object IO_Intro4 extends IOApp.Simple {

  // Thread process #1:
  val io1: IO[Int] =
    IO("task: starting").debug *>
      IO.sleep(3.seconds) *>
      IO("task: Completed").debug *>
      IO(42)

  // Wrapper for io1 - If io1Wrapper gets cancelled, extra actions
  val io1Wrapper: IO[Int] =
    io1.onCancel(IO("task: cancelled").debug.void)




  override def run: IO[Unit] =
    IO("HEHE").void               // IGNORE - JUST TO STOP RUN ERROR
}
