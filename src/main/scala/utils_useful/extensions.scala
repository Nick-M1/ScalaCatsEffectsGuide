package utils_useful

import cats.effect.IO

object extensions {

  // Method to display the thread of the process that is running
  extension [A](io: IO[A]){
    def debug: IO[A] = io.map { value =>
      println(s"[${Thread.currentThread().getName}] $value")
      value
    }
  }

}
