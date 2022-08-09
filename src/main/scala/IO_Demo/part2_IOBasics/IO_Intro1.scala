package IO_Demo.part2_IOBasics

import utils_useful.extensions.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.parallel._
import cats.syntax.apply.*

import scala.io.StdIn

/** IO Datatype:
 *  Used for dealing with values that also have side-effects
 * */

object IO_Intro1 {

  val io1: IO[Int] = IO.pure(42)      // pure() - lift a value (42) that has no side-effects into an IO
                                            //   Evaluated eagerly -> if the arg had a side-effect, then it is evaluated straight away

  val io2: IO[Int] = IO.delay({             // delay() - lift a function that has side-effects into an IO
    println("I'm doing calculation")        //   Evaluated lazily -> if the arg had a side-effect, then it is evaluated only when io2 is run
    54
  })

  val io3: IO[Int] = IO {                   // IO.apply() == IO.delay()
    println("I'm doing calculation")
    54
  }


  // In the run (or main with unsafeRunSync() method) is the only place where IOs get run.
  // Before IOs are run, they are just instructions / a chain of actions
  def main(args: Array[String]): Unit = {
    println(io1.unsafeRunSync())      // 42
    println(io2.unsafeRunSync())      // "I'm doing calculation",  54

    val io4: IO[Int] = io1.map(_ * 2)
    println(io4.unsafeRunSync())      // 84

    io1.flatMap(num => IO.delay(println(num)))    // 42
    


    // Read 2 lines from std input, then prints the lines. These are both run sequentially on the main thread (no use of io.start / fiber.join or parMapN)
    def readTwice_1(): IO[Unit] = for {           // Using for-comp
      line1 <- IO(StdIn.readLine())
      line2 <- IO(StdIn.readLine())
      _ <- IO.delay(println(line1 + line2))
    } yield ()

    def readTwice_2(): IO[Unit] = {               // Using mapN to combine IOs (higher-level function)
      (IO(StdIn.readLine()), IO(StdIn.readLine()))
        .mapN(_ + _)
        .map(println)
    }


    

    val io11: IO[Int] = IO.pure(42)
    val io12: IO[String] = IO.pure("Scala")


    def IOComposition(): IO[Unit] = for {
      _ <- io11.debug          // can save IOs to _ if dont care about their final value
      _ <- io12.debug
    } yield ()       // to return Unit

    IOComposition()
  }


}
