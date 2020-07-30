package leapfin

import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable.ListBuffer

class StatsTest extends WordSpec with Matchers {

  "Stats" should {

    "sort by elapsed time desc order" in {
      val stat1 = Stats(WorkerStatus.SUCCESS, Some(10), Some(5))
      val stat2 = Stats(WorkerStatus.SUCCESS, Some(15), Some(10))
      val stat3 = Stats(WorkerStatus.SUCCESS, Some(5), Some(15))

      val expected =
      """
        |elapsed,byte_cnt,status
        |15,10,SUCCESS
        |10,5,SUCCESS
        |5,15,SUCCESS
      """.stripMargin

      val result = Stats.createReport(ListBuffer(stat1, stat2, stat3))

      result.replaceAll("\\s+","") shouldEqual expected.replaceAll("\\s+", "")
    }

    "calc avg read by milliseconds" in {
      val stat1 = Stats(WorkerStatus.SUCCESS, Some(10), Some(5))
      val stat2 = Stats(WorkerStatus.SUCCESS, Some(15), Some(10))
      val stat3 = Stats(WorkerStatus.SUCCESS, Some(5), Some(15))


      val result = Stats.calcAvgRead(ListBuffer(stat1, stat2, stat3))

      result shouldEqual 1.0
    }

    "return zero if all stats are timed out or failed" in {
      val stat1 = Stats(WorkerStatus.TIMEOUT)
      val stat2 = Stats(WorkerStatus.FAILURE)
      val stat3 = Stats(WorkerStatus.FAILURE)

      val result = Stats.calcAvgRead(ListBuffer(stat1, stat2, stat3))

      result shouldEqual 0.0
    }
  }
}