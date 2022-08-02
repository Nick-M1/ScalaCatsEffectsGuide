package data_types.ResourceDemo

import cats.effect.{IO, Resource}
import java.io.File

/** OPENING MULTIPLE FILES: <p>
 *  Opens 2 input files & 1 output files, and copies the input files' chars to the output file <p>
 *
 *  Note: Resources released in reverse order to the acquire and that both acquire and release are non-interruptible
 *   and hence safe in the face of cancelation. <p>
 *  Outer resources will be released irrespective of failure in the lifecycle of an inner resource. <p>
 *
 *  */

object ExampleOpeningMultipleFiles {

  /*
  def openFile(str: String): IO[File] = ???
  def close(file: File): IO[Unit] = ???
  def read(file: File): Char = ???



  // Given a file's name, returns a Resource for the file
  def file(name: String): Resource[IO, File] =
    Resource.make(openFile(name))(file => close(file))        // closes file when Resource is not needed

  val concat: IO[Unit] = (
      for {
        in1 <- file("file1")        // opens files into Resources
        in2 <- file("file2")
        out <- file("file3")
      } yield (in1, in2, out)

      ).use { case (file1, file2, file3) =>
      for {
        bytes1 <- read(file1)
        bytes2 <- read(file2)
        _ <- write(file3, bytes1 ++ bytes2)
      } yield ()

    }

  /** Note: Finalization happens as soon as the use block finishes and hence the following will throw an error as the
  *    file is already closed when we attempt to read it:
  *  */

  open(file1).use(IO.pure).flatMap(readFile)

  /** As a corollary, this means that the resource is acquired every time use is invoked.
   *  So file.use(read) >> file.use(read) opens the file twice whereas file.use { file => read(file) >> read(file) } will only open it once.
   */

  */

}
