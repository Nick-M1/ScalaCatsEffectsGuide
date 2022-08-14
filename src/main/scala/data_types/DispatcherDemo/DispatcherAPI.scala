package data_types.DispatcherDemo

import scala.concurrent.duration.Duration


/** DISPATCHER: <p>
 *  Dispatcher is a fiber-based Supervisor utility for evaluating effects across an impure boundary. <p>
 *  This is useful when working with reactive interfaces that produce potentially many values (as opposed to one),
 *   and for each value, some effect in F must be performed (like inserting each value into a queue). <p>
 *
 *   Dispatcher is a kind of Supervisor and accordingly follows the same scoping and lifecycle rules with respect to submitted effects. <p>
 *   An instance of Dispatcher can be derived for any effect type conforming to the Async typeclass.
 *     
 *   */

object DispatcherAPI {

  // Dispatcher API - simplified
  abstract class DispatcherApi[F[_]] {
    
    /** Submits an effect to be executed and indefinitely blocks until a result is produced. <p>
     * This function will throw an exception if the submitted effect terminates with an error. 
     * */
    def unsafeRunSync[A](fa: F[A]): A

    /** Submits an effect to be executed and blocks for at most the specified timeout for a result to be produced. <p>
     * This function will throw an exception if the submitted effect terminates with an error. */
    def unsafeRunTimed[A](fa: F[A], timeout: Duration): A
  }


}
