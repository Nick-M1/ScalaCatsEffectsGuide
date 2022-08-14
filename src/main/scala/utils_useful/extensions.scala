package utils_useful

import cats.effect.IO

object extensions {

  /* Method to display the thread & value of the process that is running, then map it to the original value -
     doesn't have any affect on the IO or its value or any processes/functions after                           */
  extension [A](io: IO[A]){
    def debug: IO[A] = io.map { value =>
      println(s"[${Thread.currentThread().getName}] $value")
      value
    }
  }

}
