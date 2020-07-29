package finleap

import scala.collection.mutable.ListBuffer

case class Stats(status: WorkerStatus, elapsedTime: Option[Long] = None, bytesRead: Option[Long] = None) {

  def toStr: String = {
    s"${elapsedTime.getOrElse("")},${bytesRead.getOrElse("")},$status"
  }
}

object Stats {

  def calcAvgRead(stats: ListBuffer[Stats]): Double = {
    val succeeded = stats.filter(_.status == WorkerStatus.SUCCESS)
    val totalElapsed = succeeded.map(_.elapsedTime.getOrElse(0l)).sum
    val totalBytes = succeeded.map(_.bytesRead.getOrElse(0l)).sum

    if (totalElapsed == 0) 0 else totalBytes.toDouble / totalElapsed
  }

  def createReport(stats: ListBuffer[Stats]) = {
    val statsStr = stats.sortBy(_.elapsedTime)(Ordering[Option[Long]].reverse)
      .map(_.toStr)
      .mkString("\n")

    s"""
       |elapsed,byte_cnt,status
       |$statsStr
    """.stripMargin
  }
}
