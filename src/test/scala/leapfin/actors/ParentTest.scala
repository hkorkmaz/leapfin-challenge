package leapfin.actors

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import leapfin.actors.Parent.{GetStats, SaveStats, Start}
import leapfin.{PseudoRandomStream, Stats, WorkerStatus}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationDouble

class ParentTest extends TestKit(ActorSystem("parent-test-system"))
  with ImplicitSender
  with DefaultTimeout
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  "Parent" should {
    "create and start workers" in {
      val workerProbe1 = TestProbe()
      val workerProbe2 = TestProbe()

      val stream = new PseudoRandomStream()

      val parent = TestActorRef(new Parent(stream, 3 seconds, 2) {
        override def createWorkers() = List(workerProbe1.ref, workerProbe2.ref)
      })

      parent ! Start

      workerProbe1.expectMsg(Worker.Start)
      workerProbe2.expectMsg(Worker.Start)
    }

    "collect stats" in {
      val testProbe = TestProbe()

      val parent = system.actorOf(Parent.props(new PseudoRandomStream(), 5 seconds, 2))

      val stats1 = Stats(WorkerStatus.SUCCESS, Some(1), Some(2))
      val stats2 = Stats(WorkerStatus.TIMEOUT, None, None)
      val stats3 = Stats(WorkerStatus.FAILURE, None, None)

      parent ! SaveStats(stats1)
      parent ! SaveStats(stats2)
      parent ! SaveStats(stats3)

      parent ! GetStats(testProbe.ref)

      testProbe.expectMsg(ListBuffer(stats1, stats2, stats3))
    }

    "terminate when workers stop" in {
      val workerProbe1 = TestProbe()
      val workerProbe2 = TestProbe()

      val stream = new PseudoRandomStream()

      val parent = TestActorRef(new Parent(stream, 3 seconds, 2) {
        override def createWorkers() = List(workerProbe1.ref, workerProbe2.ref)
      })

      parent ! Start

      workerProbe1.ref ! PoisonPill
      workerProbe2.ref ! PoisonPill

      expectNoMessage(5 seconds)
      parent.underlying.isTerminated shouldEqual true
    }

  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}