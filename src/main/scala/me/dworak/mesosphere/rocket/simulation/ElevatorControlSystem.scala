package me.dworak.mesosphere.rocket.simulation

import me.dworak.mesosphere.rocket.model._

import scala.collection.mutable

//todo interface for a real system - this is a simulation interface
trait ElevatorControlSystem {

  def status(): Map[ElevatorId, ElevatorStatus]

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

class ElevatorSimulation(floors: Range)(implicit timeAssumption: TimeAssumption, elevatorStrategy: ElevatorStrategy)
  extends ElevatorControlSystem {

  private val elevatorStatus = mutable.Map.empty[ElevatorId, ElevatorStatus]

  override def status(): Map[ElevatorId, ElevatorStatus] = elevatorStatus.toMap

  override def status(id: ElevatorId): Option[ElevatorStatus] = elevatorStatus.get(id)

  override def update(status: ElevatorStatus): Unit = this.synchronized(elevatorStatus.put(status.elevatorId, status))

  override def pickup(sourceFloor: FloorId, up: Boolean): ElevatorId = this.synchronized {
    //decide which elevator will handle the request
    val selected = elevatorStrategy.assign(status, sourceFloor, up)
    val currentStatus = elevatorStatus(selected)
    elevatorStatus.update(selected, currentStatus.copy(destinationFloors = currentStatus.destinationFloors + sourceFloor))
    selected
  }

  override def step(): Unit = this.synchronized(elevatorStatus.transform { case (id, status) => nextStatus(status) })

  private def nextStatus(current: ElevatorStatus)(implicit timeAssumption: TimeAssumption): ElevatorStatus = {
    import me.dworak.mesosphere.rocket.model.Direction._

    current.position match {
      //reached a floor => open the door or just update position
      case Position(floor, offset, direction@(Up | Down))
        if offset + 1 == timeAssumption.ticksPerFloor && floors.contains((floor + direction.floorValueDifference).value) =>
        val newFloor = floor + direction.floorValueDifference
        val leftDestinations = current.destinationFloors - newFloor
        current.copy(
          position = current.position.copy(
            newFloor,
            Ticks.Zero,
            if (current.destinationFloors.contains(newFloor)) Open else elevatorStrategy.direction(leftDestinations, newFloor, direction)
          ),
          destinationFloors = leftDestinations
        )
      //request from current floor => open the door
      case Position(floor, Ticks(0), _) if current.destinationFloors.contains(floor) =>
        current.copy(position = current.position.copy(floor, Ticks.Zero, Open), destinationFloors = current.destinationFloors - floor)
      //closed the door
      case Position(floor, offset, Open) if offset + 1 == timeAssumption.ticksOpen =>
        current.copy(position = current.position.copy(floor, Ticks.Zero, elevatorStrategy.direction(current.destinationFloors, floor, Open)))
      //no state change => increment offset
      case Position(_, offset, Up | Down | Open) =>
        current.copy(position = current.position.copy(offset = offset + 1))
      //was waiting
      case Position(floor, _, Waiting) =>
        current.copy(position = current.position.copy(direction = elevatorStrategy.direction(current.destinationFloors, floor, Waiting)))
    }
  }
}

