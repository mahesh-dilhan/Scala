
object Function extends App {

  def sub(a: Int, b: Int): Int ={
    a-b
  }

  println(sub(2,1))

  def functionProvider(): (Int,Int) => Int ={
    sub
  }
  println(functionProvider()(7,3))

  def functionProvider1(): (Int,Int) => Int =(a:Int, b:Int) => a-b

  println(functionProvider1()(10,5))

}
