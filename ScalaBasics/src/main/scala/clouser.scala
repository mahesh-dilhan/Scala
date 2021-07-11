object clouser {

  def main(args: Array[String])
  {
    println( "Final_Sum(1) value = " + sum(1))
    println( "Final_Sum(2) value = " + sum(2))
    println( "Final_Sum(3) value = " + sum(3))

    println( "Final_Sub(1) value = " + sub(1))
    println( "Final_Sub(2) value = " + sub(2))
    println( "Final_Sub(3) value = " + sub(3))

  }

  var a = 4

  // define closure function
  val sum = (b:Int) => b + a

  val b = 10;

  val sub = (a:Int) => b - a
}

