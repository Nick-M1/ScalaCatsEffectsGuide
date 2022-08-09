package IO_Demo.part2_IOBasics

import cats.effect.{IO, IOApp}
import utils_useful.extensions.debug
import concurrent.duration.DurationInt

object IO_Intro2_ChainingIOs extends IOApp.Simple {

  /** func1, func2 & func3 are all exactly the same, just different syntax <p>
   * Show the different ways to chain IOs together
   * */

  // Chain IOs using flatMap
  def func1(): IO[Int] = {
    IO.sleep(5.seconds).flatMap(
      _ => IO(println("Hello World")).flatMap(
        _ => IO(45)
      )
    )
  }

  // Chain IOs using for-comprehension
  def func2(): IO[Int] = for {
    _ <- IO.sleep(5.seconds)
    _ <- IO(println("Hello World"))
    res <- IO(45)
  } yield res


  // Chain IOs using >> operator
  def func3(): IO[Int] = {
    IO.sleep(5.seconds)
      *> IO(println("Hello World"))
      *> IO(45)
  }


  /**
   * *> Operator: <br>
   * Runs the current IO, then runs the parameter IO, keeping its result. The result of the first action is ignored. <br>
   * If the source fails, the other action won't run. <br>
   * Not suitable for use when the parameter is a recursive reference to the current expression. <br>
   * (Strictly evaluated) <p>
   *
   *
   * >> Operator: <br>
   * Runs the current IO, then runs the parameter, keeping its result. The result of the first action is ignored. <br>
   * If the source fails, the other action won't run. <br>
   * Evaluation of the parameter is done lazily, making this suitable for recursion <p>
   *
   *
   * &> Operator: <br>
   * Runs this IO and the parameter in parallel. <br>
   * Failure in either of the IOs will cancel the other one. <br>
   * If the whole computation is canceled, both actions are also canceled.
   * (same as using both() )
   *
   * */


  override def run: IO[Unit] = {
    //    func1().debug.void
    //    func2().debug.void
    func3().debug.void

  }


}
