package data_types.ResourceDemo

import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global

object using extends App {

  // EXAMPLE 1:
  val greet: String => IO[Unit] = x => IO(println("Hello " + x))          // Function to use the resource
  Resource.eval(IO.pure("World")).use(greet).unsafeRunSync()        // Making the Resource
  // Hello World


  // EXAMPLE 2:
  /** The IOs containing Strings get combined together <br>
   *  The IOs containing printLn get printed to screen & don't get added to final value */
  val acquire: IO[String] = IO(println("Acquire cats...")) *> IO("cats")
  val release: String => IO[Unit] = _ => IO(println("...release everything"))

  val addDogs: String => IO[String] = x => IO(println("...more animals...")) *> IO.pure(x + " and dogs")
  val report: String => IO[String] = x => IO(println("...produce weather report...")) *> IO("It's raining " + x)

  val result = Resource.make(acquire)(release).evalMap(addDogs).use(report).unsafeRunSync()
  // Acquire cats...
  //...more animals...
  //...produce weather report...
  //...release everything

  println(result)
  // It's raining cats and dogs
}
