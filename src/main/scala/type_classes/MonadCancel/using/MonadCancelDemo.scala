package type_classes.MonadCancel.using

import cats.effect.MonadCancel
import cats.effect.std.Semaphore
import cats.effect.syntax.all._
import cats.syntax.all._

object MonadCancelDemo {

  def guarded[F[_], R, A, E](s: Semaphore[F], alloc: F[R])(use: R => F[A])(release: R => F[Unit])(implicit F: MonadCancel[F, E]): F[A] =
    F.uncancelable { poll => // steps inside uncancelable will continue to run & wont be interrupted if Fiber cancelled.
      for { //   poll is the value in F, if it exists

        /* flatMapping alloc (F[R]) unwraps R */
        r <- alloc

        /* poll(action) -> this action will be cancellable, inside an uncancelable function
           As this action is cancelable, have a onCancel method that performs another action (release resources) if Fiber gets canceled in the poll() */
        _ <- poll(s.acquire).onCancel(release(r))
        releaseAll = s.release >> release(r)

        /* user(action) may take a long time, so make it cancelable & handle for if Fiber does get canceled during this action
           guarantee(action) ensures releaseAll is run regardless of the result of use (whether it runs successfully, gets canceled or throws error) */
        a <- poll(use(r)).guarantee(releaseAll)
      } yield a
    }

}
