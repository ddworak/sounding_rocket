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

//todo capacity
case class ElevatorStatus(elevatorId: ElevatorId, position: Position, destinationFloors: immutable.SortedSet[FloorId])

case class FloorId(value: Int) extends Ordered[FloorId] {
  def +(floors: Int): FloorId = copy(value + floors)

  override def compare(that: FloorId): Int = value.compare(that.value)
}

case class ElevatorId(value: Int) extends AnyVal

//todo this should be part of simulation
case class Ticks(value: Int) extends AnyVal {
  def +(offset: Int): Ticks = copy(value + offset)
}

object Ticks {
  val Zero = Ticks(0)
}
