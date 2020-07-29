package finleap.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import finleap.actors.Parent.SaveStats
import finleap.actors.Worker.Start
import finleap.{DataStream, Stats, WorkerStatus}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration.DurationDouble

class WorkerTest extends TestKit(ActorSystem("worker-test-system")) with WordSpecLike with BeforeAndAfterAll {

  "Worker" should {
    "send success stats to parent" in {
      val parent = TestProbe()
      val stream = new DataStream {
        override def getData(): String = "Lpfn"
      }

      val worker = system.actorOf(Worker.props(stream, parent.ref, 3 seconds))
      worker ! Start

      parent.expectMsgType[SaveStats](5 seconds)
    }

    "send failure stats to parent" in {
      val parent = TestProbe()
      val stream = new DataStream {
        override def getData(): String = null
      }

      val worker = system.actorOf(Worker.props(stream, parent.ref, 3 seconds))
      worker ! Start

      parent.expectMsg(5 seconds, SaveStats(Stats(WorkerStatus.FAILURE, None, None)))
    }

    "send timeout stats to parent" in {
      val parent = TestProbe()

      val stream = new DataStream {
        override def getData(): String = "test"
      }

      val worker = system.actorOf(Worker.props(stream, parent.ref, 3 seconds))

      worker ! Start
      parent.expectMsg(5 seconds, SaveStats(Stats(WorkerStatus.TIMEOUT, None, None)))
    }
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
