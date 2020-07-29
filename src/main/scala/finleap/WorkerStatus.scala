package finleap

sealed trait WorkerStatus

object WorkerStatus {

  case object SUCCESS extends WorkerStatus

  case object FAILURE extends WorkerStatus

  case object TIMEOUT extends WorkerStatus

}


