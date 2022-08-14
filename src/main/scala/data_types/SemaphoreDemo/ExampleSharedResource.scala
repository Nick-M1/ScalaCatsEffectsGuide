package data_types.SemaphoreDemo

import cats.effect.{IO, IOApp, Temporal}
import cats.effect.std.{Console, Semaphore}
import cats.implicits._
import cats.effect.syntax.all._

import scala.concurrent.duration._

/** SHARED RESOURCE: <p>
 *  When multiple processes try to access the same resource and want to constrait the number of accesses -> Use Semaphore[F]. <p>
 *
 *  In this example, 3 processes are trying to access a shared resource at the same time but only one at a time will be
 *    granted access and the next process have to wait until the resource gets available again (availability is one as indicated by the semaphore counter). <p> <p>
 *
 *  R1, R2 & R3 will request access of the precious resource concurrently so this could be one possible outcome:  <br>
 *  R1 >> Availability: 1 <br>
 *  R2 >> Availability: 1 <br>
 *  R2 >> Started | Availability: 0 <br>
 *  R3 >> Availability: 0 <br>
 *  -------------------------------- <br>
 *  R1 >> Started | Availability: 0 <br>
 *  R2 >> Done | Availability: 0 <br>
 *  -------------------------------- <br>
 *  R3 >> Started | Availability: 0 <br>
 *  R1 >> Done | Availability: 0 <br>
 *  -------------------------------- <br>
 *  R3 >> Done | Availability: 1 <p><p>
 *
 *  When R1 & R2 requested the availability, there was only 1 permit available. <br>
 *  As R2 was faster in requesting access to the resource, it started processing. <br>
 *  R3 was the slowest and saw that there was no availability from the beginning. <p>
 *
 *  Once R2 was done, R1 started processing immediately showing no availability. <br>
 *  Once R1 was done, R3 started processing immediately showing no availability. <br>
 *  Finally, R3 was done showing an availability of one once again.
 * */

object ExampleSharedResource extends IOApp.Simple {

  // A single Resource-Accessor
  class PreciousResource[F[_]: Temporal](name: String, s: Semaphore[F])(implicit F: Console[F]) {
    def use: F[Unit] = for {
      x <- s.available                                      // Returns number of permits available
      _ <- F.println(s"$name >> Availability: $x")
      _ <- s.acquire                                        // Acquires a single permit (other accessors who try to access a permit if there are none left get blocked until a permit becomes available)

      y <- s.available
      _ <- F.println(s"$name >> Started | Availability: $y")
      _ <- s.release.delayBy(3.seconds)                     // After 3 secs (processing something...), releases the permit for another accessor to use

      z <- s.available
      _ <- F.println(s"$name >> Done | Availability: $z")
    } yield ()
  }

  val program: IO[Unit] = for {
    s  <- Semaphore[IO](1)                      // Initiate Semaphore, with 1 permit
    r1 = new PreciousResource[IO]("R1", s)      // Initiate 3 Resource-Accessors
    r2 = new PreciousResource[IO]("R2", s)
    r3 = new PreciousResource[IO]("R3", s)

    _  <- List(r1.use, r2.use, r3.use).parSequence.void     // Run in parallel
  } yield ()


  override def run: IO[Unit] = program



}
