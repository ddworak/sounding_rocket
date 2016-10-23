package me.dworak.mesosphere.rocket.simulation

import com.typesafe.scalalogging.LazyLogging
import me.dworak.mesosphere.rocket.model._
import me.dworak.mesosphere.rocket.strategy.ElevatorStrategy

import scala.collection.mutable

trait ElevatorSimulationControlSystem extends ElevatorControlSystem {

  def update(status: ElevatorStatus): Unit

  def step(): Unit

}

case class TimeAssumption(ticksPerFloor: Ticks, ticksOpen: Ticks) {
  require(ticksPerFloor.value > 0 && ticksOpen.value > 0)
}

class ElevatorSimulation(floors: Range)(implicit timeAssumption: TimeAssumption, elevatorStrategy: ElevatorStrategy)
  extends ElevatorSimulationControlSystem with LazyLogging {
  require(floors.size > 1)

  private val elevatorStatus = mutable.Map.empty[ElevatorId, ElevatorStatus]

  override def status(): Map[ElevatorId, ElevatorStatus] = elevatorStatus.toMap

  override def status(id: ElevatorId): Option[ElevatorStatus] = elevatorStatus.get(id)

  override def update(status: ElevatorStatus): Unit = this.synchronized(elevatorStatus.put(status.elevatorId, status))

  override def pickup(sourceFloor: FloorId, up: Boolean): ElevatorId = this.synchronized {
    //decide which elevator will handle the request
    val selected = elevatorStrategy.assign(status(), sourceFloor, up)
    val currentStatus = elevatorStatus(selected)
    elevatorStatus.update(selected, currentStatus.copy(destinationFloors = currentStatus.destinationFloors + sourceFloor))
    selected
  }

  override def step(): Unit = this.synchronized(elevatorStatus.transform { case (id, status) => nextStatus(status) })

  private def nextStatus(current: ElevatorStatus): ElevatorStatus = {
    import me.dworak.mesosphere.rocket.model.Direction._

    current.position match {
      //handle out of range case on bad update
      case Position(floor, offset, direction@(Up | Down)) if !floors.contains(floor.value + direction.floorValueDifference) =>
        logger.error(s"Invalid update caused out of range movement, will try to reset direction at ${current.elevatorId}")
        current.copy(position = current.position.copy(
          offset = Ticks.Zero,
          direction = elevatorStrategy.direction(current.destinationFloors, floor, Waiting)
        ))
      //reached a valid floor => open the door or just update position
      case Position(floor, offset, direction@(Up | Down)) if offset + 1 == timeAssumption.ticksPerFloor =>
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
      case Position(floor, offset, Up | Down | Open) =>
        current.copy(position = current.position.copy(offset = offset + 1))
      //was waiting. starts moving
      case Position(floor, _, Waiting) =>
        current.copy(position = current.position.copy(direction = elevatorStrategy.direction(current.destinationFloors, floor, Waiting)))
    }
  }
}

