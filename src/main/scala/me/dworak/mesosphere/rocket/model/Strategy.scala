package me.dworak.mesosphere.rocket.model


import scala.collection.immutable.SortedSet

trait PickupStrategy {
  def assign(status: Map[ElevatorId, ElevatorStatus], sourceFloor: FloorId, up: Boolean): ElevatorId
}

trait DirectionStrategy {
  def direction(destinations: SortedSet[FloorId], current: FloorId, previous: Direction): Direction
}

trait ElevatorStrategy extends PickupStrategy with DirectionStrategy

object SimpleElevatorStrategy extends ElevatorStrategy {
  override def direction(destinations: SortedSet[FloorId], current: FloorId, previous: Direction): Direction = {
    import Direction._
    val sameDirection = previous match {
      case Up => destinations.from(current).headOption.map(_ => Up)
      case Down => destinations.until(current).toSeq.lastOption.map(_ => Down)
      case _ => None
    }
    sameDirection.getOrElse {
      val higher = destinations.from(current)
      val lower = destinations.until(current)
      if (higher.size > lower.size) Up
      else Down
    }
    //todo waiting?
  }

  override def assign(status: Map[ElevatorId, ElevatorStatus], sourceFloor: FloorId, up: Boolean): ElevatorId = ???
}