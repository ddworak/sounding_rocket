package me.dworak.mesosphere.rocket.model

import me.dworak.mesosphere.rocket.ElevatorFixture
import me.dworak.mesosphere.rocket.model.Direction.{Up, Waiting}
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


}
