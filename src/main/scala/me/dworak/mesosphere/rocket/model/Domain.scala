package me.dworak.mesosphere.rocket.model

import scala.collection.immutable

sealed abstract class Direction(private[rocket] val floorValueDifference: Int)

object Direction {

  case object Up extends Direction(1)

  case object Down extends Direction(-1)

  case object Open extends Direction(0)

  case object Waiting extends Direction(0)

}

case class Position(floor: FloorId, offset: Ticks, direction: Direction)

case class ElevatorStatus(elevatorId: ElevatorId, position: Position, destinationFloors: immutable.SortedSet[FloorId])

case class FloorId(value: Int) extends AnyVal {
  def +(floors: Int): FloorId = copy(value + floors)
}

case class ElevatorId(value: Int) extends AnyVal

case class Ticks(value: Int) extends AnyVal {
  def +(offset: Int): Ticks = copy(value + offset)
}

object Ticks {
  val Zero = Ticks(0)
}
