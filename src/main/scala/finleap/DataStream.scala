package finleap

import scala.util.Random


trait DataStream {
  def getData(): String
}

class PseudoRandomStream extends DataStream {
  override def getData(): String = {
    val length = Random.nextInt(100)
    if (length == 19) {
      "Lpfn"
    } else {
      randomString(length)
    }
  }

  private def randomString(length: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z')
    val sb = new StringBuilder
    for (_ <- 1 to length) {
      val randomNum = Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }
}