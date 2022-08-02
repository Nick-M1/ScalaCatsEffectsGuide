package ExampleApps

import cats.effect.*
import cats.syntax.all.*

import java.io.*
import scala.concurrent.duration.*

/** COPYING FILES v3 - Making the code polymorphic <p>
 *  IO is an implementation of type-class Sync, so can use a generic type that is a Sync instead (to make code more abstract, generic & polymorphic) <br>
 *  But, leave the code in run() as IO - otherwise app won't know what Sync to use. <p>
 *
 *  Try to make code polymorphic code (except in the run method of our IOApps). <br>
 *  Polymorphic code is less restrictive, as functions are not tied to IO but are applicable to any F[_] as long as there is an instance of the type class
 *  required (Sync[F[_]] , Async[F[_]]...) in scope. The type class to use will depend on the requirements of our code.
 */

object CopyingFiles3 extends IOApp {

  def copy[F[_]: Sync](origin: File, destination: File): F[Long] =
    inputOutputStreams(origin, destination).use { case (in, out) =>
      transfer(in, out)
  }


  def inputStream[F[_]: Sync](f: File): Resource[F, FileInputStream] =
    Resource.make {
      Sync[F].blocking(new FileInputStream(f))                                        // build
    } {
      inStream => Sync[F].blocking(inStream.close()).handleErrorWith(_ => Sync[F].unit)    // release
    }

  def outputStream[F[_]: Sync](f: File): Resource[F, FileOutputStream] =
    Resource.make {
      Sync[F].blocking(new FileOutputStream(f))                                      // build
    } {
      outStream => Sync[F].blocking(outStream.close()).handleErrorWith(_ => Sync[F].unit) // release
    }



  def inputOutputStreams[F[_]: Sync](in: File, out: File): Resource[F, (InputStream, OutputStream)] = for {
    inStream  <- inputStream(in)
    outStream <- outputStream(out)
  } yield (inStream, outStream)


  def transfer[F[_]: Sync](origin: InputStream, destination: OutputStream): F[Long] =
    transmit(origin, destination, new Array[Byte](1024 * 10), 0L)

  def transmit[F[_]: Sync](origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): F[Long] = for {
    amount <- Sync[F].blocking(origin.read(buffer, 0, buffer.length))
    count  <- if(amount > -1) then Sync[F].blocking(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount)
              else Sync[F].pure(acc)
  } yield count



  override def run(args: List[String]): IO[ExitCode] = for {
    _      <- if(args.length < 2) then IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
              else IO.unit
    orig = new File(args.head)
    dest = new File(args(1))
    count <- copy[IO](orig, dest)   // [IO] to specify the type
    _     <- IO.println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}")
  } yield ExitCode.Success

}
