package me.dworak.mesosphere.rocket.model

import me.dworak.mesosphere.rocket.model.Direction.Waiting
import me.dworak.mesosphere.rocket.simulation.{ElevatorSimulation, TimeAssumption}
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.{SortedSet, mutable}

class SimpleElevatorStrategyTest extends FlatSpec with Matchers {


  trait Fixture {
    implicit val timeAssumption = new TimeAssumption {

      override def ticksOpen: Ticks = Ticks(2)

      override def ticksPerFloor: Ticks = Ticks(1)
    }

    implicit val strategy = new SimpleElevatorStrategy(1, 100)
    val system = new ElevatorSimulation()
    val initial = Seq(
      ElevatorStatus(ElevatorId(0), Position(FloorId(0), Ticks(0), Waiting), SortedSet.empty),
      ElevatorStatus(ElevatorId(1), Position(FloorId(0), Ticks(0), Waiting), SortedSet.empty),
      ElevatorStatus(ElevatorId(2), Position(FloorId(0), Ticks(0), Waiting), SortedSet.empty)
    )
    initial.foreach(system.update)
  }


  "SimpleElevatorStrategy" should "pick a random elevator" in new Fixture {

    import org.scalatest.concurrent.Eventually._

    val selectionSet = mutable.Set.empty[ElevatorId]
    eventually {
      selectionSet += system.pickup(FloorId(0), true)
      selectionSet should contain allElementsOf initial.map(_.elevatorId)
    }
  }


}
