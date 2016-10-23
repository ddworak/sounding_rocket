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

  it should "open and close the door" in new ElevatorsFixture {
    val newStatus = ElevatorStatus(ElevatorId(0), Position(FloorId(0), Ticks(0), Waiting), SortedSet(FloorId(0)))
    system.update(newStatus)
    system.step()
    system.status(newStatus.elevatorId).get.position.direction shouldBe Open
    system.step()
    system.step()
    system.status(newStatus.elevatorId).get.position.direction should not be Open
  }
}
