package data_types.RefDemo.Examples

import cats.effect.{IO, IOApp, Ref}
import cats.syntax.parallel.*
import utils_useful.extensions.*

/** WORD-COUNTER: <p>
 *  In parallel, counts the number of words in each String (from a List of Strings) && finds the total number of words
 * */
object WordCounter extends IOApp.Simple {

  /** Method #1: <p>
   *  Impure function with mutability & is not thread-safe -> BAD */
  def impureConcurrent_v1(): IO[Unit] = {
    var count = 0             // total number of words -> mutable (BAD)

    def task(workload: String): IO[Unit] = {
      val wordCount = workload.split(" ").length                // count the number of words in this String
      for {
        _ <- IO(s"Counting words for $workload: $wordCount").debug
        newCount = count + wordCount
        _ <- IO(s"New total: $newCount").debug
        _ = count = newCount
      } yield ()
    }

    List("I like Cats Effects", "Hello world", "Scala is cool")
      .map(task)            // For each String in this List[String]
      .parSequence          // run in parallel for each String
      .void
  }

  // TODO: impureConcurrent_v1 is wrong


  /** Method #2:
   *
   * Making impureConcurrent_v1 pure with IOs
   *
   * This is still bad because:
   *  - hard to read/debug
   *  - mixing pure & impure code
   *  - not thread safe
   *  */
  def impureConcurrent_v2(): IO[Unit] = {
    var count = 0             // mutable (BAD), but for demonstration

    def task(workload: String): IO[Unit] = {
      val wordCount = workload.split(" ").length
      for {
        _ <- IO(s"Counting words for $workload: $wordCount").debug
        newCount <- IO(count + wordCount)            // Wrap mutable code in IO & flatMap instead of =
        _ <- IO(s"New total: $newCount").debug
        _ <- IO(count += wordCount)                     // Wrap mutable code in IO & flatMap instead of =
      } yield ()
    }

    List("I like Cats Effects", "Hello world", "Scala is cool")
      .map(task)
      .parSequence
      .void
  }

  /** Method #3: <p>
   * Rewriting impureConcurrent() in a pure way, using Ref for thread-safety <p>
   * Concurrent & thread-safe reads/writes over shared values
   * */
  def pureConcurrent(): IO[Unit] = {

    // Use recursion
    def task(workload: String, total: Ref[IO, Int]): IO[Unit] = {       // Pass accumulator for recursion (this is the Ref) - removes mutable count variable
      val wordCount = workload.split(" ").length                        // word count of current String
      for {
        _ <- IO(s"Counting words for $workload: $wordCount").debug                   // displaying
        newCount <- total.updateAndGet(currentCount => currentCount + wordCount)     // Similar to total += wordCount of current word
        _ <- IO(s"New total: $newCount").debug                                       // displaying
      } yield ()
    }

    for {                                           // As Ref is being used, use in a for-comp (for easier syntax)
      initialCount <- Ref[IO].of(0)                 // initialise Ref
      _ <- List("I like Cats Effects", "Hello world", "Coding in scala is cool")
            .map(str => task(str, initialCount))    // for each String in List, apply method task() to it, and run all Strings in parallel
            .parSequence
    } yield ()


    
  }

  override def run: IO[Unit] = {
//    impureConcurrent_v2()
    pureConcurrent()
  }

}
