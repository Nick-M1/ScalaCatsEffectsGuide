package data_types.Random

import cats.effect.IO
import cats.effect.std.Random
import cats.Functor
import cats.syntax.functor._

/** RANDOM: <br>
 *  A purely-functional implementation of a source of random information. <br>
 *  The API of Random has all the methods of the Scala standard library Random <br>
 *
 *  Cats-effects Random can choose what source it gets its randomness from:
 *  - scala.util.Random (Random.scalaUtilRandom[F: Sync]) -> can set initial seed or load balanced between several Random instances
 *  - java.util.Random
 *  - java.security.SecureRandom
 * */

object create {

  // Simplified Random API
  trait myRandom[F[_]] {
    def nextInt: F[Int]
    def shuffleList[A](list: List[A]): F[List[A]]
    // ... and more
  }



  // Creating a Random instance:
  Random.scalaUtilRandom[IO]

  // Using Random:
  def dieRoll[F[_]: Functor: Random]: F[Int] =
    Random[F].betweenInt(0, 6).map(_ + 1)       // `6` is excluded from the range




}
