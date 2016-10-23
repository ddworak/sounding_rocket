package me.dworak.mesosphere.rocket

trait ElevatorControlSystem {
  def status(): Seq[(Int, Int, Int)]

  def update(elevatorId: Int, sourceFloor: Int, destinationFloor: Int): Unit

  def pickup(sourceFloor: Int, destinationFloor: Int): Unit

  def step(): Unit
}
