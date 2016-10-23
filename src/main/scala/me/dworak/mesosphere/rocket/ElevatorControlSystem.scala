package me.dworak.mesosphere.rocket

trait ElevatorControlSystem {
  def status: Map[ElevatorId, ElevatorState]

  def status(elevatorId: ElevatorId): Option[ElevatorState]

  def update(elevatorState: ElevatorState): Unit

  def pickup(sourceFloor: FloorId, up: Boolean): Unit

  def step(): Unit
}

case class FloorId(value: Int) extends AnyVal

case class ElevatorId(value: Int) extends AnyVal

case class ElevatorState(elevatorId: ElevatorId, currentFloor: FloorId, destinationFloors: List[FloorId])