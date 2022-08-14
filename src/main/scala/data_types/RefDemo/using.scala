package data_types.RefDemo

import cats.effect.{IO, IOApp, Ref}
import utils_useful.extensions.*
import cats.syntax.parallel.*

object using {

  // Ref = purely functional atomic reference wrapper - ALWAYS THREAD-SAFE
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


}
