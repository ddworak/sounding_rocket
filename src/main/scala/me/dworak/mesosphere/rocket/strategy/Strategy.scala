package me.dworak.mesosphere.rocket.strategy

import me.dworak.mesosphere.rocket.model.{Direction, ElevatorId, ElevatorStatus, FloorId}

import scala.collection.immutable.SortedSet

trait PickupStrategy {
  def assign(status: Map[ElevatorId, ElevatorStatus], sourceFloor: FloorId, up: Boolean): ElevatorId
}

trait DirectionStrategy {
  def direction(destinations: SortedSet[FloorId], current: FloorId, previous: Direction): Direction
}

trait ElevatorStrategy extends PickupStrategy with DirectionStrategy