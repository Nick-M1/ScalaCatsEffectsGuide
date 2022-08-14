package data_types.CyclicBarrierDemo

import cats.implicits._
import cats.effect._
import cats.effect.std.CyclicBarrier
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration._

// Using Cats-effects:
object Demo1 extends App {
  
  /** Overview: <p>
   *  Set fibers to wait (get blocked) on this CyclicBarrier (with CyclicBarrier.await) <p>
   *  When the capacity of CyclicBarrier is reached (in this case, when 2 fibers are 'awaiting' on CyclicBarrier), <br>
   *    then it signals all the fibers to carry on.
   * */
  val run: IO[Unit] = for {
    // Initialises a CyclicBarrier with a capacity of 2        
    b <- CyclicBarrier[IO](2)                
    
    // Run the faster fiber - will 'reach' / start waiting for the CyclicBarrier 1st
    f1 <- (IO.println("fast fiber before barrier") >>
            b.await >>                          // At this point, this fiber will wait until the Cyclic barrier has its max capcity of fibers waiting on it (2 in this case)
            IO.println("fast fiber after barrier")
          ).start                               // start the 1st fiber

    // Run the slower fiber - will 'reach' / start waiting for the CyclicBarrier 2nd
    f2 <- (IO.sleep(1.second) >>
            IO.println("slow fiber before barrier") >>
            IO.sleep(1.second) >>
            b.await >>                          // Will 'reach' the CyclicBarrier. As the CyclicBarrier will reach max capacity (of 2), then it will unblock all the fibers waiting on it
            IO.println("slow fiber after barrier")
          ).start

    _ <- (f1.join, f2.join).tupled      // Get the results of both fibers
    
  } yield ()


  run.unsafeRunSync()

}
