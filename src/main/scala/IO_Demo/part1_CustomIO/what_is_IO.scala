package IO_Demo.part1_CustomIO

object what_is_IO {

  /** MyIO:
   *  - Describes any computation that might produce side-effects
   *  - Calculates a value of type A, if it is successful
   *  - Side-effects are required for the evaluation of () => A
   *
   * MyIO contains:
   *  - Generic type A (this is what the final value of this IO will return when it is run - could be Unit for no returned value)
   *  - unsafeRun (a function (chain of steps/actions) that has no inputs, outputs the return value of type A and might have side-effects)
   *  - Methods map & flatMap (myIO is a Monad)
   * */

  case class MyIO[A](unsafeRun: () => A) {      // Note: For our user-defined MyIO, the syntax for unsafeRun is slightly different to Cat-Effect's IO
    def map[B](f: A => B): MyIO[B] =
      MyIO(() => f(unsafeRun()))

    def flatMap[B](f: A => MyIO[B]): MyIO[B] =
      MyIO(() => f(unsafeRun()).unsafeRun())
  }


  // Example of myIO
  val io1: MyIO[Int] = MyIO(() => {
    println("I'm writing something...") // Side-effect
    42 // return value (int) or chain of actions to get the return value
  })

  def main(args: Array[String]): Unit =
    io1.unsafeRun() // display("I'm printing something")
    println(io1.unsafeRun()) // res = 40,    && display("I'm printing something")

}
