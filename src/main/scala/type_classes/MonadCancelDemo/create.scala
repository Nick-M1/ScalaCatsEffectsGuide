package type_classes.MonadCancelDemo

import cats.MonadError
import cats.effect.Poll

object create {
  
  trait myMonadCancel[F[_], E] extends MonadError[F, E] {
    def canceled: F[Unit]
    def uncancelable[A](poll: Poll[F] => F[A]): F[A]
    
  }
}
