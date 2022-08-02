package data_types.ResourceDemo

/** RESOURCE <p>
 *  For acquiring a resource (e.g. a file or socket), performing some action on it and then running a finalizer (eg closing the file handle), regardless of the outcome of the action. <p>
 *
 *  Resource forms a MonadError on the resource type and is based on Bracket, but with extra functionality & simpler syntax <p>
 *
 *  Nested resources are released in reverse order of acquisition. Outer resources are released even if an inner acquisition, use or release fails.
 * */

object create {

  // Simplified Resource API:
  object myResource {
    def make[F[_], A](acquire: F[A])(release: A => F[Unit]): myResource[F, A] = ???     // Constructs a Resource
    def eval[F[_], A](fa: F[A]): myResource[F, A] = ???                                 // Lift arbitrary actions to resources
  }

  abstract class myResource[F[_], A] {
    def use[B](f: A => F[B]): F[B]                                              // Consume a Resource
  }
}
