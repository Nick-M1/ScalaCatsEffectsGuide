package data_types.BracketDemo

import java.io._
import java.io.BufferedReader
import cats.effect.IO
import cats.effect.Outcome.{Succeeded, Errored, Canceled}
import utils_useful.extensions.debug


object using {

  /** Imperative approach */
  def javaReadFirstLine(file: File): String = {
    val in = new BufferedReader(new FileReader(file))
    try {
      in.readLine()
    } finally {       // finally executes regardless of the outcome of try
      in.close()
    }
  }

  /**
   * USING CATS-EFFECTS BRACKET: <br>
   * * Is pure, so it can be used for FP <br>
   * * Works with asynchronous IO actions <br>
   * * Release action will happen regardless of the exit status of the use action (e.g. for successful completion, thrown errors or canceled execution) <br>
   * * If use action throws an error and then the release action throws an error as well, the reported error will be that of use, whereas the error thrown by release will just get logged (via System.err)
   * */
  def readFirstLine(file: File): IO[String] =
    IO(new BufferedReader(new FileReader(file)))    // The resource
      .bracket { in =>
        IO(in.readLine())                           // Usage (the try block)
      } { in =>
        IO(in.close()).void                         // Releasing the reader (the finally block) - runs no matter the outcome of the Usage action
      }


  /** USING CATS-EFFECTS BRACKET-CASE:
   *
   *  Same as Bracket, but returns an ExitCase in release, in order to distinguish between:
   *  - successful completion -> Succeeded()
   *  - completion in error   -> Errored()
   *  - cancellation          -> Canceled()
   *  */
  def readLine(in: BufferedReader): IO[String] =
    IO.pure(in).bracketCase { in =>
      IO(in.readLine())
    } {
      case (_, Succeeded(_)) => IO.unit                                             // Do nothing with the resource
      case (_, Errored(_) | Canceled()) => IO("Failed").debug >> IO(in.close())     // Resource failed, so close it
    }

}
