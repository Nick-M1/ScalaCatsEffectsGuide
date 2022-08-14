package type_classes.SpawnDemo

import cats.effect.{MonadCancel, Spawn}
import cats.effect.syntax.all._
import cats.syntax.all._

/** Establishing connection to a server */
object Demo1 {

  trait Server[F[_]] {
    def accept: F[Connection[F]]
  }

  trait Connection[F[_]] {
    def read: F[Array[Byte]]
    def write(bytes: Array[Byte]): F[Unit]
    def close: F[Unit]
  }

  def endpoint[F[_]: Spawn](server: Server[F])(body: Array[Byte] => F[Array[Byte]]): F[Unit] = {

    def handle(conn: Connection[F]): F[Unit] = for {
      request <- conn.read
      response <- body(request)
      _ <- conn.write(response)
    } yield ()

    /** Handles a single request <p>
     *  Uses uncancelable (from MonadCancel) to avoid resource leaks in the interval between when we get a connection (conn) and when we set up the resource management to ensure that it is properly closed<p>
     *  server.accept -> connects to server <p>
     *  guarantee(conn.close) -> ensures connection always closes at end, no matter if previous stages fail <p>
     *
     *  start -> takes an effect F[A] and returns an effect which produces a new Fiber[F, E, A], where E is the error type for F (usually Throwable).
     *    The Fiber type is a running fiber: it is actually executing as soon as you have the Fiber instance in your hand. This means that it's running in the background, which is to say, it is a separate semantic thread.
     */
    val handler = MonadCancel[F].uncancelable { poll =>
      poll(server.accept).flatMap { conn =>
        handle(conn).guarantee(conn.close).start
      }
    }

    // run handler indefinitely
    handler.foreverM
  }

}
