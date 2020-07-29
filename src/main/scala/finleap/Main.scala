package finleap

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import finleap.actors.Parent
import finleap.actors.Parent.Start

import scala.concurrent.duration.FiniteDuration

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("system")

  val conf = ConfigFactory.load

  val workerCount = conf.getInt("workerCount")
  val timeout = FiniteDuration(conf.getInt("timeout"), TimeUnit.SECONDS)

  val stream = new PseudoRandomStream()
  val parent = system.actorOf(Parent.props(stream, timeout, workerCount), "parent")

  system.log.info("Starting application, worker count -> {}, timeout -> {}", workerCount, timeout)

  parent ! Start
}
