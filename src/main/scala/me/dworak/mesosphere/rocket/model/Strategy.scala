package me.dworak.mesosphere.rocket.model

import scala.collection.immutable.SortedSet

trait PickupStrategy {
  def assign(status: Map[ElevatorId, ElevatorStatus], sourceFloor: FloorId, up: Boolean): ElevatorId
}

trait DirectionStrategy {
  def direction(floors: SortedSet[FloorId], previous: Direction): Direction
}

trait ElevatorStrategy extends PickupStrategy with DirectionStrategy