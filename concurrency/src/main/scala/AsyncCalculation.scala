import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import scala.language.postfixOps

object AsyncCalculation extends App {

  implicit val baseTime = System.currentTimeMillis


  val f = Future {
    sleep(500)
    1 + 1
  }


  val result = Await.result(f, 1 second)
  println(result)
  sleep(1000)

  def sleep(time: Long) { Thread.sleep(time) }
}

