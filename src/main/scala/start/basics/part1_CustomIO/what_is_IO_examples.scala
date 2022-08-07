package start.basics.part1_CustomIO

import scala.io.StdIn
import start.basics.part1_CustomIO.what_is_IO.MyIO

object what_is_IO_examples extends App {

  // #1 - An IO which returns the current time of the system
  val clockIO: MyIO[Long] = MyIO(() => System.currentTimeMillis())
  clockIO.unsafeRun() // current time in millis...


  // #2 - An IO which measures the duration of a computation (measure1 & measure2 are exactly the same)
  def measure1[A](computation: MyIO[A]): MyIO[Long] = for {
    startTime <- clockIO // gets start time
    _ <- computation // runs computation
    finishTime <- clockIO // gets end time
  } yield finishTime - startTime

  def measure2[A](computation: MyIO[A]): MyIO[Long] = {
    clockIO.flatMap(startTime =>
      computation.flatMap(_ =>
        clockIO.map(finishTime => finishTime - startTime)
      )
    )
  }


  def testMeasure(): Unit = {
    val test = measure1(MyIO(() => Thread.sleep(1000)))
    println(test.unsafeRun())
  }

  testMeasure() // 1018


  // #3 - An IO which prints to the console
  def putStrLn(line: String): MyIO[Unit] =
    MyIO(() => println(line))


  // #4 - An IO which reads a line (string) from the std input
  val read: MyIO[String] = MyIO(() => StdIn.readLine())


  // Testing #3 & #4:
  def testConsole(): Unit = {
    val program: MyIO[Unit] = for { // return MyIO[Unit] when there isn't a final value at the end of IO action
      line1 <- read // Reads 2 lines from std input (line1 & line2 are Strings)
      line2 <- read
      _ <- putStrLn(line1 + line2) // Prints these 2 lines (flatMap to _, as this function doesn't return a value we want to save)
    } yield ()

    program.unsafeRun()
  }

  testConsole() // std input: "Hello" "World" -> res = "HelloWorld"

}
