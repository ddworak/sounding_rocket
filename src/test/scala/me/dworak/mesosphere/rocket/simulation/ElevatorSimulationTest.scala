package me.dworak.mesosphere.rocket.simulation

import me.dworak.mesosphere.rocket.ElevatorFixture
import me.dworak.mesosphere.rocket.model.Direction.{Down, Open, Waiting}
import me.dworak.mesosphere.rocket.model._
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.SortedSet

class ElevatorSimulationTest extends FlatSpec with Matchers {

  "ElevatorSimulation" should "update status" in new ElevatorFixture {
    val all = system.status()
    val newStatus: ElevatorStatus = ElevatorStatus(ElevatorId(0), Position(FloorId(10), Ticks(0), Down), SortedSet.empty)
    system.update(newStatus)
    system.status(newStatus.elevatorId) should contain(newStatus)
    system.status shouldBe all.updated(newStatus.elevatorId, newStatus)

  }

  it should "update status on pickup request" in new ElevatorFixture {
    assume(!system.status().exists { case (id, status) => status.destinationFloors.nonEmpty })
    system.pickup(FloorId(2), true)
    system.status().count { case (id, status) => status.destinationFloors.nonEmpty } shouldBe 1
  }

  it should "open and close the door after assumed time" in new ElevatorFixture {
    val newStatus = ElevatorStatus(ElevatorId(0), Position(FloorId(0), Ticks(0), Waiting), SortedSet(FloorId(0)))
    system.update(newStatus)
    system.step()
    system.status(newStatus.elevatorId).get.position.direction shouldBe Open
    (0 until timeAssumption.ticksOpen.value).foreach(_ => system.step())
    system.status(newStatus.elevatorId).get.position.direction should not be Open
  }

  it should "visit all set floors in estimated ticks for simple scenario" in new ElevatorFixture {
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

  it should "handle invalid update graciously" in new ElevatorFixture {
    val outOfRangeDown = ElevatorStatus(ElevatorId(0), Position(FloorId(0), Ticks(0), Down), SortedSet(FloorId(2)))
    system.update(outOfRangeDown)
    var i = 0
    while (system.status(outOfRangeDown.elevatorId).get.destinationFloors.nonEmpty) {
      i += 1
      system.step()
    }
    i shouldBe 1 + 2 * timeAssumption.ticksPerFloor.value


    val outOfRangeUp = ElevatorStatus(ElevatorId(0), Position(FloorId(0), Ticks(0), Down), SortedSet(FloorId(2)))
    system.update(outOfRangeUp)
    i = 0
    while (system.status(outOfRangeUp.elevatorId).get.destinationFloors.nonEmpty) {
      i += 1
      system.step()
    }
    i shouldBe 1 + 2 * timeAssumption.ticksPerFloor.value
  }
}
