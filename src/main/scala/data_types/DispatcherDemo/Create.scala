package data_types.DispatcherDemo


/** DISPATCHER: <p>
 *  Dispatcher is a fiber-based Supervisor utility for evaluating effects across an impure boundary. <p>
 *  This is useful when working with reactive interfaces that produce potentially many values (as opposed to one),
 *   and for each value, some effect in F must be performed (like inserting each value into a queue). <p>
 *
 *   An instance of Dispatcher can be derived for any effect type conforming to the Async typeclass.
 *   */

object Create {

  // Dispatcher API - simplified
  abstract class ImpureInterface {
    def onMessage(msg: String): Unit      // User implements this method themselves

    def init(): Unit = {
      onMessage("init")
    }

    // ...
  }


}
