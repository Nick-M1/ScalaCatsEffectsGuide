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

    /** Returns the next pseudorandom, uniformly distributed value between min (inclusive) and max (exclusive) from this random number generator's sequence */
    def betweenDouble(minInclusive: Double, maxExclusive: Double): F[Double]
    def betweenFloat(minInclusive: Float, maxExclusive: Float): F[Float]
    def betweenInt(minInclusive: Int, maxExclusive: Int): F[Int]
    def betweenLong(minInclusive: Long, maxExclusive: Long): F[Long]


    /** Returns a pseudorandomly chosen element */
    def nextAlphaNumeric: F[Char]           // returns alphanumeric character (from A-Z, a-z or 0 - 9).
    def nextPrintableChar: F[Char]          // returns char from the ASCII range 33-126
    def nextString(length: Int): F[String]  // returns random String

    def nextBoolean: F[Boolean]             // returns true or false
    def nextBytes(n: Int): F[Array[Byte]]   // Generates n random bytes and returns them in a new array.

    def nextInt: F[Int]
    def nextLong: F[Long]
    def nextDouble: F[Double]               // returns double between 0.0 and 1.0
    def nextFloat: F[Float]                 // returns float between 0.0 and 1.0

    def nextGaussian: F[Double]             // returns Gaussian ("normally") distributed double value with mean 0.0 and standard deviation 1.0


    /** Returns a pseudorandom, uniformly distributed value between 0 (inclusive) and the specified value (exclusive), drawn from this random number generator's sequence. */
    def nextIntBounded(n: Int): F[Int]
    def nextLongBounded(n: Long): F[Long]


    /** Returns a new collection of the same type with the elements in a random order. */
    def shuffleList[A](l: List[A]): F[List[A]]
    def shuffleVector[A](v: Vector[A]): F[Vector[A]]

  }








}
