package me.dworak.mesosphere.rocket

trait ElevatorControlSystem {
  def status: Map[ElevatorId, ElevatorStatus]

  def status(elevatorId: ElevatorId): Option[ElevatorStatus]

  def update(elevatorState: ElevatorStatus): Unit

  def pickup(sourceFloor: FloorId, up: Boolean): ElevatorId

  def step(): Unit
}

case class FloorId(value: Int) extends AnyVal

case class ElevatorId(value: Int) extends AnyVal

case class Ticks(value: Int) extends AnyVal

sealed trait Direction

object Direction {

  case object Up extends Direction

  case object Down extends Direction

  case object Open extends Direction

}

trait TimeAssumption {
  def ticksPerFloor: Ticks

  def ticksOpen: Ticks
}

case class Position(floor: FloorId, offset: Ticks, direction: Direction)

case class ElevatorStatus(elevatorId: ElevatorId, position: Position, destinationFloors: List[FloorId])