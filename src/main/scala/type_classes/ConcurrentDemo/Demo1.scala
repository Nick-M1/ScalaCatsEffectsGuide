package type_classes.ConcurrentDemo

import cats.effect.{IO, Deferred}
import cats.effect.unsafe.implicits.global

/** COUNT DOWN LATCH: <p>
 *  Instantiated with n > 0 latches and allows fibers to semantically block until all n latches are released.  <p>
 *
 *  */

object Demo1 {

  // Represent State
  sealed trait State[F[_]]
  case class Awaiting[F[_]](latches: Int, signal: Deferred[F, Unit]) extends State[F]
  case class Done[F[_]]() extends State[F]


//  def await[F[_]]: F[Unit] = state.get.flatMap {
//      case Awaiting(_, signal) => signal.get
//      case Done() => F.unit
//    }

  // https://typelevel.org/cats-effect/docs/typeclasses/concurrent
  // .............
}
