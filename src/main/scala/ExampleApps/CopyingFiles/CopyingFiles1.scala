package ExampleApps.CopyingFiles

import cats.effect.*
import scala.concurrent.duration.*
import java.io.*

/** COPYING FILES - Basic concepts, resource handling & cancellation
 *
 */

object CopyingFiles1 extends IOApp {

  /** For opening/closing files & reading/writing content <p>
   * /   Returns IO that returns number of bytes copied as Long. The steps for this effect will be in IO (to keep purity) <br>
   * No exception should be thrown outside this IO for this function, instead the IO instance should just fail */
  def copy(origin: File, destination: File): IO[Long] =
    inputOutputStreams(origin, destination).use { case (in, out) =>
      transfer(in, out)
    }


  /** Acquiring and releasing Resources: <p>
   * Opening a stream is a side-effectful action, so encapsulate those actions in their own IO instances. <br>
   * Embed actions by calling IO(action), but with input/output actions use IO.blocking(action). <br>
   * IO.blocking(action) means that the action wont be cancelled if the IO in cancelled <br>
   * If want the IO instance to be interrupted when cancelled, without waiting for it to be finished, use IO.interruptible <p>
   *
   * Cats-effect's Resource allows us to orderly create, use and then release resources. <br>
   * It opens the stream, allows us to perform actions on the stream and safely closes the streams */

  def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.make {
      IO.blocking(new FileInputStream(f)) // build
    } {
      inStream => IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit) // release
    }

  def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make {
      IO.blocking(new FileOutputStream(f)) // build
    } {
      outStream => IO.blocking(outStream.close()).handleErrorWith(_ => IO.unit) // release
    }


  /** Encapsulates both Resources into a single Resource instance, that is available once & only if both streams are successfully open <br>
   * Resources can be used in for-comps as they have flatMap method
   *
   * Note: when releasing resources we must handle any errors during a stream release, for example with the .handleErrorWith call (as above). <br>
   * Here, we just ignore the error, but we should log it. The method .attempt.void is used to get the same 'swallow and ignore errors' behavior.
   *
   * If input file couldnt be opened then the output file will even attempt to be opened.  (inputStream opened 1st, then outputStream) <br>
   * If output file cant be opened, then the input stream will be closed. */

  def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] = for {
    inStream <- inputStream(in)
    outStream <- outputStream(out)
  } yield (inStream, outStream)


  /** transfer will do the actual logic of copying data, once resources (streams) are obtained successfully. <br>
   * When they are not needed anymore, (whatever the outcome of transfer - success or failure) both streams will be closed. <br>
   * If any of the streams could not be obtained, then transfer will not be run.
   *
   * transfer will use a recursive (stack-safe - due to using IO) function transmit to traverse each byte in the input file & write this byte to the output file,
   * and at the same time will store the total number of bytes transferred so far (val acc).<br>
   * for-comp used as flatMap on an IO (Monad) will 'unwrap' its value (this case the value is Int for amount & Long for count)
   *
   * */
  def transfer(origin: InputStream, destination: OutputStream): IO[Long] =
    transmit(origin, destination, new Array[Byte](1024 * 10), 0L)

  def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] = for {
    amount <- IO.blocking(origin.read(buffer, 0, buffer.length))
    count <- if (amount > -1) then IO.blocking(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount)
    else IO.pure(acc) // End of read stream reached (by java.io.InputStream contract), nothing to write
  } yield count // Returns the actual amount of bytes transmitted // Returns the actual amount of bytes transmitted


  /** run method is like a main method in normal Scala, but for Cats-effects <p>
   * Note:  Any interruption (like pressing Ctrl-c) will be treated as a cancellation of the running IO.
   * */
  override def run(args: List[String]): IO[ExitCode] = for { // args is the input stream (could be user std input, but here it is the input file)

    // If the length of input file is < 2 -> throw exception, else carry on (by doing IO.unit, which will just chain this step with the next steps into a single IO using flatMap)
    _ <- if (args.length < 2) then IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
    else IO.unit
    orig = new File(args.head) // args.head == args(0) - Getting the 1st index of args List[String]
    dest = new File(args(1))
    count <- copy(orig, dest)
    _ <- IO.println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}")
  } yield ExitCode.Success // run must end with ExitCode

}
