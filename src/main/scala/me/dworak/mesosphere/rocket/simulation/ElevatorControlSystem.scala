package me.dworak.mesosphere.rocket.simulation

import me.dworak.mesosphere.rocket.model._

import scala.collection.mutable

trait ElevatorControlSystem {
  def status: Map[ElevatorId, ElevatorStatus]

  def status(id: ElevatorId): Option[ElevatorStatus]

  def update(status: ElevatorStatus): Unit

  /**
    * Pickup request.
    *
    * @param sourceFloor source floor
    * @param up          true when destination is higher than source floor
    * @return elevator handling the request
    */
  def pickup(sourceFloor: FloorId, up: Boolean): ElevatorId

  def step(): Unit
}

trait TimeAssumption {
  def ticksPerFloor: Ticks

  def ticksOpen: Ticks
}

class ElevatorSimulation(implicit timeAssumption: TimeAssumption, elevatorStrategy: ElevatorStrategy)
  extends ElevatorControlSystem {

  private val elevatorStatus = mutable.Map.empty[ElevatorId, ElevatorStatus]

  override def status: Map[ElevatorId, ElevatorStatus] = elevatorStatus.toMap

  override def status(id: ElevatorId): Option[ElevatorStatus] = elevatorStatus.get(id)

  override def update(status: ElevatorStatus): Unit = this.synchronized(elevatorStatus.put(status.elevatorId, status))

  override def pickup(sourceFloor: FloorId, up: Boolean): ElevatorId = this.synchronized {
    //decide which elevator will handle the request
    val selected = elevatorStrategy.assign(status, sourceFloor, up)
    val currentStatus = elevatorStatus(selected)
    elevatorStatus.update(selected, currentStatus.copy(destinationFloors = currentStatus.destinationFloors + sourceFloor))
    selected
  }

  override def step(): Unit = this.synchronized(elevatorStatus.mapValues(status => nextStatus(status)))

  private def nextStatus(current: ElevatorStatus)(implicit timeAssumption: TimeAssumption): ElevatorStatus = {
    import me.dworak.mesosphere.rocket.model.Direction._

    if (current.destinationFloors.isEmpty) current.copy(position = current.position.copy(offset = Ticks.Zero, direction = Waiting))
    else current.position match {
      case Position(floor, offset, direction@(Up | Down)) if offset + 1 == timeAssumption.ticksPerFloor =>
        current.copy(position = current.position.copy(floor + direction.floorValueDifference, Ticks.Zero, elevatorStrategy.direction(current.destinationFloors - floor, direction)))
      case Position(floor, offset, Open) if offset + 1 == timeAssumption.ticksOpen =>
        current.copy(position = current.position.copy(floor, Ticks.Zero, elevatorStrategy.direction(current.destinationFloors, Open)))
      case Position(_, offset, Up | Down | Open) =>
        current.copy(position = current.position.copy(offset = offset + 1))
      case Position(_, _, Waiting) => //destinationFloors non-empty
        current.copy(position = current.position.copy(direction = elevatorStrategy.direction(current.destinationFloors, Waiting)))
    }
  }
}

