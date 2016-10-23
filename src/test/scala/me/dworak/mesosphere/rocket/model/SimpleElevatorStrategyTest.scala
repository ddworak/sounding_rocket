package me.dworak.mesosphere.rocket.model

import me.dworak.mesosphere.rocket.ElevatorFixture
import me.dworak.mesosphere.rocket.model.Direction.{Down, Open, Up, Waiting}
import org.scalatest.concurrent.Eventually
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.{immutable, mutable}


class SimpleElevatorStrategyTest extends FlatSpec with Matchers with Eventually {

  import scala.concurrent.duration._

  implicit val config = PatienceConfig(2.seconds, 1.seconds)

  "SimpleElevatorStrategy" should "pick a random elevator" in new ElevatorFixture {

    val selectionSet = mutable.Set.empty[ElevatorId]
    eventually {
      selectionSet += system.pickup(FloorId(0), true)
      selectionSet should contain allElementsOf initial.map(_.elevatorId)
    }
  }

  it should "prefer current direction over distance and number of destinations below/above" in new ElevatorFixture {
    val id = ElevatorId(0)
    system.update(ElevatorStatus(id, Position(FloorId(0), Ticks.Zero, Waiting), immutable.SortedSet(FloorId(5))))
    system.step()
    system.step()
    system.step()
    val position = system.status(id).get.position
    position.direction shouldBe Up
    position.floor shouldBe FloorId(2)
    system.pickup(FloorId(0), true)
    system.pickup(FloorId(1), true)
    system.pickup(FloorId(4), true)
    system.step()
    system.status(id).get.position.direction shouldBe Up

    eventually {
      system.step()
      val status = system.status(id).get
      status.destinationFloors shouldBe immutable.SortedSet.empty[FloorId]
      system.status(id).get.position.direction shouldBe Waiting
    }
  }

  it should "prefer waiting and open elevators" in new ElevatorFixture {
    system.update(ElevatorStatus(ElevatorId(0), Position(FloorId(2), Ticks.Zero, Waiting), immutable.SortedSet.empty))
    system.update(ElevatorStatus(ElevatorId(1), Position(FloorId(2), Ticks.Zero, Up), immutable.SortedSet(FloorId(3))))
    system.update(ElevatorStatus(ElevatorId(2), Position(FloorId(2), Ticks.Zero, Open), immutable.SortedSet(FloorId(3))))

    (0 to 1000).foreach { _ =>
      val assigned = system.pickup(FloorId(2), true)
      Seq(assigned) should contain oneOf(ElevatorId(0), ElevatorId(2))
      assigned should not be ElevatorId(1)
    }

  }

  it should "prefer piggybacking" in new ElevatorFixture {
    system.update(ElevatorStatus(ElevatorId(0), Position(FloorId(3), Ticks.Zero, Waiting), immutable.SortedSet.empty))
    system.update(ElevatorStatus(ElevatorId(1), Position(FloorId(3), Ticks.Zero, Up), immutable.SortedSet.empty))
    system.update(ElevatorStatus(ElevatorId(2), Position(FloorId(0), Ticks.Zero, Up), immutable.SortedSet(FloorId(3))))
    system.update(ElevatorStatus(ElevatorId(3), Position(FloorId(1), Ticks.Zero, Down), immutable.SortedSet(FloorId(0))))

    system.pickup(FloorId(2), true) shouldBe ElevatorId(2)
  }


}
