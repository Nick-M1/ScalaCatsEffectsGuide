package data_types.ResourceDemo

import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration._
import cats.effect.std.Random
import cats.syntax.all._

object using extends App {

  // EXAMPLE 1:
  val greet: String => IO[Unit] = x => IO(println("Hello " + x))          // Function to use the resource
  Resource.eval(IO.pure("World")).use(greet).unsafeRunSync()        // Making the Resource
  // Hello World

  println("\n----------------------------------------------------------\n")


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

  println("\n----------------------------------------------------------\n")



  // EXAMPLE 3: Opening multiple resources in parallel
  def mkResource(name: String) = {

    val acquire = for {
      random <- Random.scalaUtilRandom[IO]
      randInt <- random.nextIntBounded(1000)      // Get a random int thats less than 1000
      _ <- IO.sleep(randInt.millis)               // Sleep this random time
      _ <- IO.println(s"Acquiring $$name")
    } yield name                                  // The final value of this IO[String] is 'name'

    def release(name: String) =
      IO.println(s"Releasing $$name")

    Resource.make(acquire)(release)               // When Resource initialised, performs 'aquire' function then when Resource no longer being used, performs 'release' function
  }

  val r = (mkResource("one"), mkResource("two"))
    .parMapN((s1, s2) => s"I have $s1 and $s2")   // Opening/initialising these Resources in parallel -> runs the 'aquire' function above
    .use(IO.println(_))                           // After this, the Resources are released & the 'release' function is run

  r.unsafeRunSync()
  // Acquiring $name
  // Acquiring $name
  // I have one and two
  // Releasing $name
  // Releasing $name

  println("\n----------------------------------------------------------\n")


  // EXAMPLE 4:

  val a: IO[Unit] = IO(println("This is pre-allocated"))
  val b: IO[String] = IO(println("Resource opened")) *> IO("RESOURCE")

  val c: String => IO[String] = x => IO(println("Using the resource")) *> IO(s"$x IS USED")

  val d: String => IO[String] = x => IO(println("Transforming the resource")) *> IO(s"$x IS TRANSFORMED")
  val e: String => IO[String] = x => IO(println("Further transforming the resource")) *> IO(s"$x IS THE SAME AS AT d")

  val f: String => IO[Unit] = x => IO(println("Resource is released"))
  val g: IO[Unit] = IO(println("Finaliser"))

  val ex3 = Resource.make(b)(f)   // b opens the resource. f is function to release the resource
    .preAllocate(a)     // function that happens before resource is opened
    .evalMap(d)         // applies a flatMap to the resource (aka the result of 'c') & modifies the value of the resource for the functions after
    .evalTap(e)         // applies a flatMap to the resource (aka the result of 'd'), but doesn't modify the value of the resource for the functions after
    .onFinalize(g)      // Runs after resource has been released
    .use(c)             // function to actually use the resource
    .unsafeRunSync()    // Runs this whole chain of effects/actions (could use IOApp's run function instead)

  // This is pre-allocated
  // Resource opened
  // Transforming the resource
  // Further transforming the resource
  // Using the resource
  // Resource is released
  // Finaliser


  println(ex3)      // >> RESOURCE IS TRANSFORMED IS USED


}
