package me.dworak.mesosphere.rocket.model

trait ElevatorControlSystem {

  def status(): Map[ElevatorId, ElevatorStatus]

  def status(id: ElevatorId): Option[ElevatorStatus]

  /**
    * Pickup request.
    *
    * @param sourceFloor source floor
    * @param up          true when destination is higher than source floor
    * @return elevator handling the request
    */
  def pickup(sourceFloor: FloorId, up: Boolean): ElevatorId

}
