import cats.effect.{IO, IOApp}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import concurrent.ExecutionContext.Implicits.global
import utils_useful.extensions.debug
import cats.effect.Async
import cats.effect.implicits
import cats.effect.IO.asyncForIO
import cats.implicits._

object TESTING2 extends IOApp.Simple {


  override def run: IO[Unit] = ???
}
