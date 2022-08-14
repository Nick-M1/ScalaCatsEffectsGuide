package data_types.Random

import cats.effect.IO
import cats.effect.std.Random
import cats.Functor
import cats.syntax.functor._

object Demo {

  // Creating a Random instance:
  Random.scalaUtilRandom[IO]

  // Using Random:
  def dieRoll[F[_] : Functor : Random]: F[Int] =
    Random[F].betweenInt(0, 6).map(_ + 1) // `6` is excluded from the range

}
