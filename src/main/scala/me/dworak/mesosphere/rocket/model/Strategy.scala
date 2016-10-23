package me.dworak.mesosphere.rocket.model


import me.dworak.mesosphere.rocket.model.Direction.{Down, Up, Waiting}

import scala.collection.immutable.SortedSet
import scala.util.Random

trait PickupStrategy {
  def assign(status: Map[ElevatorId, ElevatorStatus], sourceFloor: FloorId, up: Boolean): ElevatorId
}

trait DirectionStrategy {
  def direction(destinations: SortedSet[FloorId], current: FloorId, previous: Direction): Direction
}

trait ElevatorStrategy extends PickupStrategy with DirectionStrategy

class SimpleElevatorStrategy(distanceMultiplier: Double, directionMultiplier: Double) extends ElevatorStrategy {
  override def direction(destinations: SortedSet[FloorId], current: FloorId, previous: Direction): Direction = {
    import Direction._
    val higher = destinations.from(current) - current //from has inclusive bound
    val lower = destinations.until(current)
    val sameDirection = previous match {
      case Up if higher.nonEmpty => Some(Up)
      case Down if lower.nonEmpty => Some(Down)
      case _ => None
    }
    sameDirection.getOrElse {
      if (higher.size > lower.size) Up
      else Down
    }
    //todo waiting?
  }

  override def assign(status: Map[ElevatorId, ElevatorStatus], sourceFloor: FloorId, up: Boolean): ElevatorId = {
    def distance(elevatorStatus: ElevatorStatus): Double =
      math.abs(elevatorStatus.position.floor.value - sourceFloor.value) * distanceMultiplier
    def direction(elevatorStatus: ElevatorStatus): Double =
      (elevatorStatus.position match {
        case Position(floor, _, Waiting) if floor == sourceFloor => -10
        case Position(floor, _, Up) if floor < sourceFloor => -1 + (if (up) -1 else 0)
        case Position(floor, _, Down) if floor > sourceFloor => -1 + (if (!up) -1 else 0)
        case Position(floor, _, Up) if floor > sourceFloor => 1
        case Position(floor, _, Down) if floor < sourceFloor => 1
        case _ => 0
      }) * directionMultiplier

    val (_, mins) = status
      .values
      .groupBy(status => distance(status) + direction(status))
      .min(Ordering.by[(Double, Iterable[ElevatorStatus]), Double](_._1))
    mins.toSeq(Random.nextInt(mins.size)).elevatorId
  }
}