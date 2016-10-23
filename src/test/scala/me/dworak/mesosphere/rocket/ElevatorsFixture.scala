package me.dworak.mesosphere.rocket

import me.dworak.mesosphere.rocket.model.Direction.Waiting
import me.dworak.mesosphere.rocket.model._
import me.dworak.mesosphere.rocket.simulation.{ElevatorSimulation, TimeAssumption}

import scala.collection.SortedSet

trait ElevatorsFixture {
  implicit val timeAssumption = TimeAssumption(Ticks(1), Ticks(2))

  implicit val strategy = new SimpleElevatorStrategy(1, 100)
  val system = new ElevatorSimulation(0 to 10)
  val initial = Seq(
    ElevatorStatus(ElevatorId(0), Position(FloorId(0), Ticks(0), Waiting), SortedSet.empty),
    ElevatorStatus(ElevatorId(1), Position(FloorId(0), Ticks(0), Waiting), SortedSet.empty),
    ElevatorStatus(ElevatorId(2), Position(FloorId(0), Ticks(0), Waiting), SortedSet.empty)
  )
  initial.foreach(system.update)
}
