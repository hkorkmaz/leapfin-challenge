package finleap.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import finleap.actors.Parent.{GetStats, SaveStats, Start}
import finleap.{DataStream, Stats}

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

class Parent(dataStream: DataStream, timeout: FiniteDuration, workerCount: Integer) extends Actor with ActorLogging {
  private var activeWorkerCount = workerCount
  private var workerStats = mutable.ListBuffer.empty[Stats]

  private val workers = createWorkers()

  override def receive: Receive = {
    case Start =>
      workers.foreach(_ ! Worker.Start)
    case SaveStats(incoming) =>
      workerStats = workerStats :+ incoming
    case GetStats(receiver) =>
      receiver ! workerStats
    case Terminated(_) =>
      workerStopped()
  }

  private def workerStopped() = {
    activeWorkerCount = activeWorkerCount - 1
    log.info("Worker terminated, active worker count -> {}", activeWorkerCount)

    if (activeWorkerCount == 0) {
      printStats()
      self ! PoisonPill
    }
  }

  def createWorkers() = {
    log.info("Starting workers")

    (1 to workerCount).map { i =>
      context.actorOf(Worker.props(dataStream, self, timeout), s"worker-$i")
    }.toList
  }

  private def printStats() = {
    log.info("Printing stats -> {}", workerStats)
    println(Stats.createReport(workerStats))

    val avgRead = Stats.calcAvgRead(workerStats)
    println(s"Avg Bytes/Ms: ${"%.02f".format(avgRead)}")
  }

  override def preStart() = {
    super.preStart()
    workers.foreach(context.watch)
  }

  override def postStop() = {
    super.postStop()

    log.info("Stopping application")
    context.system.terminate()
  }
}

object Parent {

  sealed trait Message

  case object Start extends Message

  final case class SaveStats(stats: Stats) extends Message

  final case class GetStats(receiver: ActorRef) extends Message

  def props(dataStream: DataStream, timeout: FiniteDuration, workerCount: Integer) = Props.create(classOf[Parent], dataStream, timeout, workerCount)
}
