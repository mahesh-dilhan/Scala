import scala.concurrent.{Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.util.Random

import scala.language.postfixOps
object OnSuccessAndFailure extends App {
  def sleep(time: Long) { Thread.sleep(time) }
  val f = Future {
    sleep(Random.nextInt(500))
    if (Random.nextInt(500) > 250) throw new Exception("Yikes!") else 42
  }
  val mapped = f.transform {
    case Success(_) => Success("OK")
    case Failure(_) => Success("KO")
  }


  // do the rest of your work
  println("A ..."); sleep(100)
  println("B ..."); sleep(100)
  println("C ..."); sleep(100)
  println("D ..."); sleep(100)
  println("E ..."); sleep(100)
  println("F ..."); sleep(100)
  sleep(200)
}