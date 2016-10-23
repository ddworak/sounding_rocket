package me.dworak.mesosphere.rocket.simulation

import me.dworak.mesosphere.rocket.model._
import me.dworak.mesosphere.rocket.simulation.ElevatorControlSystem.DirectionStrategy

import scala.collection.immutable.SortedSet
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

  /**
    * Step is a two-stage operation:
    * - apply any pending requests
    * - time-step one tick forward
    */
  def step(): Unit
}

object ElevatorControlSystem {
  type DirectionStrategy = SortedSet[FloorId] => Direction
}


trait TimeAssumption {
  def ticksPerFloor: Ticks

  def ticksOpen: Ticks
}


class ElevatorSimulation(implicit timeAssumption: TimeAssumption, directionStrategy: DirectionStrategy) extends ElevatorControlSystem {

  private val elevatorStatus = mutable.Map.empty[ElevatorId, ElevatorStatus]

  override def status: Map[ElevatorId, ElevatorStatus] = elevatorStatus.toMap

  override def status(id: ElevatorId): Option[ElevatorStatus] = elevatorStatus.get(id)

  override def update(status: ElevatorStatus): Unit = ???

  override def pickup(sourceFloor: FloorId, up: Boolean): ElevatorId = ???

  override def step(): Unit = ???

  private def nextStatus(current: ElevatorStatus)(implicit timeAssumption: TimeAssumption, directionStrategy: DirectionStrategy): ElevatorStatus = {
    import me.dworak.mesosphere.rocket.model.Direction._

    if (current.destinationFloors.isEmpty) current.copy(position = current.position.copy(offset = Ticks.Zero, direction = Waiting))
    else current.position match {
      case Position(floor, offset, direction@(Up | Down)) if offset + 1 == timeAssumption.ticksPerFloor =>
        current.copy(position = current.position.copy(floor + direction.floorValueDifference, Ticks.Zero, directionStrategy(current.destinationFloors - floor)))
      case Position(floor, offset, Open) if offset + 1 == timeAssumption.ticksOpen =>
        current.copy(position = current.position.copy(floor, Ticks.Zero, directionStrategy(current.destinationFloors)))
      case Position(_, offset, Up | Down | Open) =>
        current.copy(position = current.position.copy(offset = offset + 1))
      case Position(_, _, Waiting) => //destinationFloors non-empty
        current.copy(position = current.position.copy(direction = directionStrategy(current.destinationFloors)))
    }
  }
}

