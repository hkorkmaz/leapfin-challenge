package finleap.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import finleap.actors.Parent.SaveStats
import finleap.actors.Worker.{Start, Stop, Tick}
import finleap.{DataStream, Stats, WorkerStatus}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationDouble, FiniteDuration}
import scala.language.postfixOps
import scala.util.Try


class Worker(stream: DataStream, parent: ActorRef, timeout: FiniteDuration) extends Actor with ActorLogging {
  private var bytesRead = 0l
  private var ticker: Option[Cancellable] = None
  private var timer: Option[Cancellable] = None
  private val startTime = System.currentTimeMillis()

  override def receive = {
    case Start => start()
    case Tick => processStream()
    case Stop => stop()
  }

  private def start() = {
    log.info("Worker started")
    timer = Option(context.system.scheduler.scheduleOnce(timeout, self, Stop))
    ticker = Option(context.system.scheduler.schedule(0 millisecond, 200 millisecond, self, Tick))
  }

  private def processStream() = Try {
    val data = stream.getData()
    bytesRead = bytesRead + data.getBytes().length

    if (data.contains("Lpfn")) {
      val stats = Stats(WorkerStatus.SUCCESS, Some(elapsedTime), Some(bytesRead))
      log.info("Worker successful, sending stats -> {}", stats)
      parent ! SaveStats(stats)
      self ! PoisonPill
    }
  } recover {
    case ex: Exception =>
      log.error("Error while processing stream -> {}", ex)
      parent ! SaveStats(Stats(WorkerStatus.FAILURE))
      self ! PoisonPill
  }

  private def stop() = {
    val stats = Stats(WorkerStatus.TIMEOUT)
    log.info("Worker timed out, sending stats -> {}", stats)
    parent ! SaveStats(stats)
    self ! PoisonPill
  }

  override def postStop() = {
    super.postStop()
    ticker.foreach(_.cancel())
    ticker = None

    timer.foreach(_.cancel())
    timer = None
  }

  private def elapsedTime = System.currentTimeMillis() - startTime
}


object Worker {

  sealed trait Message

  case object Start extends Message

  private case object Stop extends Message

  private case object Tick extends Message

  def props(stream: DataStream, parent: ActorRef, timeout: FiniteDuration): Props = Props.create(classOf[Worker], stream, parent, timeout)
}
