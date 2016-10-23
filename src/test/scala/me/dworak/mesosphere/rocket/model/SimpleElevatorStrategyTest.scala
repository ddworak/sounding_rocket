package me.dworak.mesosphere.rocket.model

import me.dworak.mesosphere.rocket.ElevatorsFixture
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class SimpleElevatorStrategyTest extends FlatSpec with Matchers {


  "SimpleElevatorStrategy" should "pick a random elevator" in new ElevatorsFixture {

    import org.scalatest.concurrent.Eventually._

    val selectionSet = mutable.Set.empty[ElevatorId]
    eventually {
      selectionSet += system.pickup(FloorId(0), true)
      selectionSet should contain allElementsOf initial.map(_.elevatorId)
    }
  }


}
