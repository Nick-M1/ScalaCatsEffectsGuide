package Case_Studies.CopyingFiles

import cats.effect.*
import cats.syntax.all.*

import java.io.*
import scala.concurrent.duration.*

/** COPYING FILES v2 - Using Bracket instead of Resource
 *
 * Resource is based on Bracket, but automates getting & freeing resources.<br>
 * (this can also be done with Bracket, but more manual code)
 */

object CopyingFiles2 {

  // transfer will do the real work
  def transfer(origin: InputStream, destination: OutputStream): IO[Long] = ???


  /** 3 Stages when using Bracket:
   *   - Resource acquisition (opening streams)
   *   - Usage (copying data)
   *   - Release (freeing resources, closing streams)
   *
   * The Release stage will always be run, regardless if the Usage stage finished successfully <br>
   * However, if the input file opened & there was an error opening output file, then this Release stage wont run (so input stream wont close) -> ISSUE <br>
   * To solve this, use a separate bracket for the input & output streams (but this is what we did before with Resource)
   *
   * Resource, however, will always go to the release/freeing stage even if the IO is cancelled or a previous stage failed.
   */
  def copy(origin: File, destination: File): IO[Long] = {
    val inIO: IO[InputStream] = IO(new FileInputStream(origin))
    val outIO: IO[OutputStream] = IO(new FileOutputStream(destination))

    (inIO, outIO) // Stage 1: Getting resources
      .tupled // From (IO[InputStream], IO[OutputStream]) to IO[(InputStream, OutputStream)]
      .bracket {
        case (in, out) =>
          transfer(in, out) // Stage 2: Using resources (for copying data, in this case)
      } {
        case (in, out) => // Stage 3: Freeing resources
          (IO(in.close()), IO(out.close()))
            .tupled // From (IO[Unit], IO[Unit]) to IO[(Unit, Unit)]
            .handleErrorWith(_ => IO.unit).void
      }
  }
}
