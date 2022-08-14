package data_types.ResourceDemo

import cats.{Applicative, Functor}
import cats.effect.kernel.Resource
import cats.effect.kernel.Resource.ExitCase
import cats.effect.{MonadCancel, Spawn, Concurrent}

/** RESOURCE <p>
 *  For acquiring a resource (e.g. a file or socket), performing some action on it and then running a finalizer (eg closing the file handle), regardless of the outcome of the action. <p>
 *
 *  Resource forms a MonadError on the resource type and is based on Bracket, but with extra functionality & simpler syntax <p>
 *
 *  Nested resources are released in reverse order of acquisition. Outer resources are released even if an inner acquisition, use or release fails.
 * */

object ResourceAPI {

  // Simplified Resource API:
  abstract class myResource[F[_], A] {

    /** Allocates a resource and supplies it to the given function. <p>
     * The resource is released as soon as the resulting F[B] is completed, whether normally or as a raised error. */
    def use[B](f: A => F[B]): F[B]

    /**
     * Allocates a resource with a non-terminating use action. <br>
     * Useful to run programs that are expressed entirely in `Resource`. <p>
     * The finalisers run when the resulting program fails or gets interrupted.
     */
    def useForever(implicit F: Spawn[F]): F[Nothing]

    /**
     * Allocates a resource and closes it immediately.
     */
    def use_(implicit F: MonadCancel[F, Throwable]): F[Unit] = use(_ => F.unit)


    /**
     * Allocates two resources concurrently, and combines their results in a tuple.
     *
     * The finalizers for the two resources are also run concurrently with each other, but within _each_ of the two resources, nested finalizers are run in the usual reverse order of acquisition.
     *
     * Note that `Resource` also comes with a `cats.Parallel` instance that offers more convenient access to the same functionality as `both`, for example via `parMapN`
     */
    def both[B](that: Resource[F, B])(implicit F: Concurrent[F]): Resource[F, (A, B)]

    /**
     * Races the evaluation of two resource allocations and returns the result of the winner, except in the case of cancelation.
     */
    def race[B](that: Resource[F, B])(implicit F: Concurrent[F]): Resource[F, Either[A, B]]


    /**
     * Applies an effectful transformation to the allocated resource. <p>
     * Like a `flatMap` on `F[A]` while maintaining the resource context
     */
    def evalMap[B](f: A => F[B]): Resource[F, B]

    /**
     * Applies an effectful transformation to the allocated resource. <p>
     * Like a `flatTap` on `F[A]` while maintaining the resource context
     */
    def evalTap[B](f: A => F[B]): Resource[F, A]

    /**
     * Runs `precede` before this resource is allocated.
     */
    def preAllocate(precede: F[Unit]): Resource[F, A]

    /**
     * Runs `finalizer` when this resource is closed. Unlike the release action passed to
     * `Resource.make`, this will run even if resource acquisition fails or is canceled.
     */
    def onFinalize(finalizer: F[Unit])(implicit F: Applicative[F]): Resource[F, A]

    /**
     * Like `onFinalize`, but the action performed depends on the exit case.
     */
    def onFinalizeCase(f: ExitCase => F[Unit])(implicit F: Applicative[F]): Resource[F, A]

  }



  // Companion object for Resource
  object myResource {

    /** Creates a resource from an allocating effect. */
    def apply[F[_], A](resource: F[(A, F[Unit])])(implicit F: Functor[F]): Resource[F, A] = ???

    /**
     * Creates a resource from an allocating effect, with a finalizer that is able to distinguish between [[ExitCase exit cases]].
     */
    def applyCase[F[_], A](resource: F[(A, ExitCase => F[Unit])]): Resource[F, A] = ???

    /**
     * Given a `Resource` suspended in `F[_]`, lifts it in the `Resource` context.
     */
    def suspend[F[_], A](fr: F[Resource[F, A]]): Resource[F, A] =
      Resource.eval(fr).flatMap(x => x)

    /**
     * Creates a resource from an acquiring effect and a release function.
     */
    def make[F[_], A](acquire: F[A])(release: A => F[Unit])(implicit F: Functor[F]): Resource[F, A] = ???

    /**
     * Creates a resource from an acquiring effect and a release function that can discriminate between different [[ExitCase exit cases]].
     */
    def makeCase[F[_], A](acquire: F[A])(release: (A, ExitCase) => F[Unit])(implicit F: Functor[F]): Resource[F, A] = ???

    /**
     * Allocates two resources concurrently, and combines their results in a tuple.
     */
    def both[F[_] : Concurrent, A, B](rfa: Resource[F, A], rfb: Resource[F, B]): Resource[F, (A, B)] = ???

    /**
     * Races the evaluation of two resource allocations and returns the result of the winner, except in the case of cancelation.
     */
    def race[F[_] : Concurrent, A, B](rfa: Resource[F, A], rfb: Resource[F, B]): Resource[F, Either[A, B]] = ???
  }


}
