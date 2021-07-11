object PartialFn {

  def main(args: Array[String]): Unit = {

    val sample = 1 to 10
    val isEven: PartialFunction[Int, String] = {
      case x if x % 2 == 0 => x+" is even"
    }

    // the method collect can use isDefinedAt to select which members to collect
    val evenNumbers = sample collect isEven

    println(evenNumbers)

    val isOdd: PartialFunction[Int, String] = {
      case x if x % 2 == 1 => x+" is odd"
    }

    // the method orElse allows chaining another partial function to handle
    // input outside the declared domain
    val numbers = sample map (isEven orElse isOdd)
    println(numbers)
  }


}
