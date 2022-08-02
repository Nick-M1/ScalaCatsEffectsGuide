package data_types.RefDemo

import cats.effect.{IO, IOApp, Ref}
import utils_useful.extensions.*
import cats.syntax.parallel.*

object using extends IOApp.Simple {

  // Ref = purely functional atomic reference wrapper - ALWAYS THREAD-SAFES
  val ref1: IO[Ref[IO, Int]] = Ref[IO].of(42)     // Ref[A, B] -> A = effect type, B = value type
  val ref2: IO[Ref[IO, Int]] = IO.ref(42)         // Easier syntax than ref1 (ref1 == ref2)


  // Modifying is an effect
  val ref3: IO[Unit] = ref1.flatMap { ref =>
    ref.set(43)                  // sets the value in Ref to a new value
  }

  // Obtain a value
  val ref4: IO[Int] = ref1.flatMap { ref =>
    ref.get                     // returns the value in ref (no modification)
  }

  val ref5: IO[Int] = ref1.flatMap { ref =>
    ref.getAndSet(43)           // sets the value to 43, and returns the old value (42)
  }

  // Updating with a function
  val ref6: IO[Unit] = ref1.flatMap { ref =>
    ref.update(value => value * 10)             // new value in ref = 420
  }

  val ref7: IO[Int] = ref1.flatMap { ref =>
    ref.updateAndGet(value => value * 10)        // update with a function && returns new value (420)
    // Use GetAndUpdate(...)      to update & return old value
  }

  // Modify with a function & return a different type
  val ref8: IO[String] = ref1.flatMap { ref =>
    ref.modify(value => (value * 10, s"current value is $value"))    // like update, but returns a tuple where the 2nd element is of a different type (e.g. a string)
  }



  // WHY:
  // Concurrent & thread-safe reads/writes over shared values, in a purely functional way

  def impureConcurrent_v1(): IO[Unit] = {
    var count = 0             // mutable (BAD), but for demonstration

    def task(workload: String): IO[Unit] = {
      val wordCount = workload.split(" ").length                // a task using mutability
      for {
        _ <- IO(s"Counting words for $workload: $wordCount").debug
        newCount = count + wordCount
        _ <- IO(s"New total: $newCount").debug
        _ = count = newCount
      } yield ()
    }

    List("I like Cats Effects", "Hello world", "Scala is cool")
      .map(task)
      .parSequence
      .void
  }

  // Making impureConcurrent_v1 pure with IOs
  /** This is still bad because:
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


  // Rewriting impureConcurrent() in a pure way, using Ref for thread-safety
  def pureConcurrent(): IO[Unit] = {

    def task(workload: String, total: Ref[IO, Int]): IO[Unit] = {       // Pass accumulator for recursion (this is the Ref) - removes mutable count variable
      val wordCount = workload.split(" ").length
      for {
        _ <- IO(s"Counting words for $workload: $wordCount").debug
        newCount <- total.updateAndGet(currentCount => currentCount + wordCount)        // Similar to total += wordCount of current word
        _ <- IO(s"New total: $newCount").debug
      } yield ()
    }

    for {
      initialCount <- Ref[IO].of(0)
      _ <- List("I like Cats Effects", "Hello world", "Coding in scala is cool")
            .map(str => task(str, initialCount))
            .parSequence
    } yield ()




  }



  override def run: IO[Unit] = {
//    impureConcurrent_v2()
    pureConcurrent()
  }

}
