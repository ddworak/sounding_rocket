package me.dworak.mesosphere.rocket.simulation

import me.dworak.mesosphere.rocket.ElevatorsFixture
import me.dworak.mesosphere.rocket.model.Direction.{Down, Open, Waiting}
import me.dworak.mesosphere.rocket.model._
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.SortedSet

class ElevatorSimulationTest extends FlatSpec with Matchers {

  "ElevatorSimulation" should "update status" in new ElevatorsFixture {
    val all = system.status()
    val newStatus: ElevatorStatus = ElevatorStatus(ElevatorId(0), Position(FloorId(10), Ticks(0), Down), SortedSet.empty)
    system.update(newStatus)
    system.status(newStatus.elevatorId) should contain(newStatus)
    system.status shouldBe all.updated(newStatus.elevatorId, newStatus)

  }

  it should "open and close the door after assumed time" in new ElevatorsFixture {
    val newStatus = ElevatorStatus(ElevatorId(0), Position(FloorId(0), Ticks(0), Waiting), SortedSet(FloorId(0)))
    system.update(newStatus)
    system.step()
    system.status(newStatus.elevatorId).get.position.direction shouldBe Open
    (0 until timeAssumption.ticksOpen.value).foreach(_ => system.step())
    system.status(newStatus.elevatorId).get.position.direction should not be Open
  }

  it should "visit all set floors in estimated ticks for simple scenario" in new ElevatorsFixture {
    val newStatus = ElevatorStatus(ElevatorId(0), Position(FloorId(0), Ticks(0), Waiting), SortedSet(FloorId(0), FloorId(2), FloorId(5), FloorId(10)))
    system.update(newStatus)
    var i = 0
    while (system.status(newStatus.elevatorId).get.destinationFloors.nonEmpty) {
      i += 1
      system.step()
    }
    val perFloor = timeAssumption.ticksPerFloor.value
    val perOpen = timeAssumption.ticksOpen.value
    i shouldBe 1 + perOpen + 2 * perFloor + perOpen + 3 * perFloor + perOpen + 5 * perFloor
  }
}
