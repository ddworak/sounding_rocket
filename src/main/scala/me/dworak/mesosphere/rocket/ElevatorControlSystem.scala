package me.dworak.mesosphere.rocket

import me.dworak.mesosphere.rocket.Direction.{Down, Open, Up, Waiting}

import scala.collection.immutable.SortedSet

trait ElevatorControlSystem {
  def status: Map[ElevatorId, ElevatorStatus]

  def status(elevatorId: ElevatorId): Option[ElevatorStatus]

  def update(elevatorState: ElevatorStatus): Unit

  def pickup(sourceFloor: FloorId, up: Boolean): ElevatorId

  def step(): Unit
}

object ElevatorControlSystem {
  type DirectionStrategy = SortedSet[FloorId] => Direction
}

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

sealed abstract class Direction(private[rocket] val floorValueDifference: Int)

object Direction {

  case object Up extends Direction(1)

  case object Down extends Direction(-1)

  case object Open extends Direction(0)

  case object Waiting extends Direction(0)

}

trait TimeAssumption {
  def ticksPerFloor: Ticks

  def ticksOpen: Ticks
}


case class Position(floor: FloorId, offset: Ticks, direction: Direction)

case class ElevatorStatus(elevatorId: ElevatorId, position: Position, destinationFloors: SortedSet[FloorId]) {
  def nextStatus()(implicit timeAssumption: TimeAssumption, directionStrategy: ElevatorControlSystem.DirectionStrategy): ElevatorStatus =
    if (destinationFloors.isEmpty) copy(position = position.copy(offset = Ticks.Zero, direction = Waiting))
    else position match {
      case Position(floor, offset, direction@(Up | Down)) if offset + 1 == timeAssumption.ticksPerFloor =>
        copy(position = position.copy(floor + direction.floorValueDifference, Ticks.Zero, directionStrategy(destinationFloors - floor)))
      case Position(floor, offset, Open) if offset + 1 == timeAssumption.ticksOpen =>
        copy(position = position.copy(floor, Ticks.Zero, directionStrategy(destinationFloors)))
      case Position(_, offset, Up | Down | Open) =>
        copy(position = position.copy(offset = offset + 1))
      case Position(_, _, Waiting) => //destinationFloors non-empty
        copy(position = position.copy(direction = directionStrategy(destinationFloors)))
    }
}