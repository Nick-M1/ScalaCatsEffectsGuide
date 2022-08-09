package data_types.ClockDemo

import scala.concurrent.duration.TimeUnit


/** CLOCK: <p>
 *
 * Clock provides the current time, as a pure alternative to: <br>
 * * Java's System.currentTimeMillis & System.nanoTime <br>
 * * JavaScript's Date.now() and performance.now() <p>
 *
 * The reason to use Clock: <br>
 * * Its functions are pure, with the results suspended in F[_] <br>
 * * Requiring this data type as a restriction means that code using Clock can have alternative implementations injected; for example time passing can be simulated in tests, such that time-based logic can be tested much more deterministically and with better performance, without actual delays happening
 * */
object create {

  // Simplified API for Clock:
  trait myClock[F[_]] {
    def realTime(unit: TimeUnit): F[Long]     // for getting the "real-time clock" - Alternative to Java's System.currentTimeMillis
    def monotonic(unit: TimeUnit): F[Long]    // for time measurements             - Alternative to Java's System.nanoTime

  }

}
