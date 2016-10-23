#Sounding Rocket
######[the only way to get to mesosphere](https://en.wikipedia.org/wiki/Mesosphere#Uncertainties)
##A simple (4-hour) elevator dispatching simulation

### The Problem
Design and implement an elevator control system. 
Your elevator control system should be able to handle a few elevators — up to 16.

In the end, your control system should provide an interface for:

* Querying the state of the elevators (what floor are they on and where they are going),
* receiving an update about the status of an elevator,
* receiving a pickup request,
* time-stepping the simulation.

### Interface
I've decided to revamp the interface a little bit. 

The biggest change is inspired by The Rule of Two sponsored by the Order of the Sith Lords 
(and my design decision to split logic from simulation):
> "Two there should be; no more, no less. One to embody power, the other to crave it."
>
> — Darth Bane

The resulting traits look like this:
```scala
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

trait ElevatorSimulationControlSystem extends ElevatorControlSystem {

  def update(status: ElevatorStatus): Unit

  def step(): Unit

}
```
Other minor changes:
* some value classes so I don't waste 2/4 hours debugging parameter order
* batch status is a map for easy querying
* additional single elevator status method
* pickups return assigned elevator ID

What I'd do if there was more time:
* explicit asynchronicity of requests 
```scala
  def pickup(sourceFloor: FloorId, up: Boolean): Future[ElevatorId]
```
sometimes there are no elevators available, often something goes wrong, generally real life is hard, 
so it would be nice if the interface induced these thoughts in its users.
* (requires previous point) nice request status with estimation

### The Solution
The solution is split into three parts, so it only makes sense to split its description in a similar manner.
#### Domain
`me.dworak.mesosphere.rocket.model`

All the domain classes: floors, direction, elevators, statuses and the interface definition of a control system. 
An important enum to know when reading this code is `Direction`, which resembles current objective(or a lack of it)
of an elevator.

One issue I did not manage to address during given timeframe was the `Ticks` dependency for status, 
but it's quite simple to go for more abstract statuses here and make it more specific only in simulations.

Oh, and did I mention capacity? I did not implement that here, but I should, elevators tend to be crowded. 
In Poland we often say, that one more person will always fit, but this rule has some ugly inductive implications.

#### Simulation
`me.dworak.mesosphere.rocket.simulation`

A framework for running elevator simulations. 
Can be easily configured with:
* `TimeAssumption` - how long reaching a floor and opening/closing the door should take
* `ElevatorStrategy` - more about those in the next chapter

`ElevatorSimulation` dispatches pickup requests and makes sure all states are changed on `step()` 
according to provided strategy and time settings. The core state change logic is implemented in `nextStatus()` method.

Independently of the settings, there are basically 5 transitions:

1. Upon reaching a floor (through movement) either open the door or keep moving
2. On request from current floor open the door
3. Once enough time passes, close the door and move/wait.
4. Unless enough time passed, just change the time(ticks) offset.
5. A Waiting elevator checks whether there are some new destinations and transitions to a move.

There's also some sanity checking about ranges (since `update(status)` could basically change anything), 
but I'd need more tests to bet my life on this system's correctness. Way more tests.

Overall the simulation proved to be quite convenient to work with, though there are some things I'd work on. 

E.g.: pickup requests are assumed to be resolved immediately, which makes them impossible to depend on next elevator state.
The user has to think about his `step()` times. I'd love to see this work  more reactively, without timestepping,
although this would make the testing harder.

#### Strategy
`me.dworak.mesosphere.rocket.strategy`

A complete `ElevatorStrategy` consists of two strategies - one for choosing direction (based on destination floors)
and another one for assigning elevators to pickup requests. 
In my very limited (yet hopefully working) implementation I've tried to use a simple set of rules with easily customizable
prioritization.
I try to stay moving the same direction as much as possible, since it would be extremely inconvenient for the passengers
already moving up to have to move down when someone enters and decides to go there. I've seen that in my faculty building
and it made me use the stairs more (though I see how it might be the intended result to keep us healthy). 
When in doubt, the elevators go to the more popular direction and when idle they just switch to waiting. I've marked the
line of code where this could become a nice optimization with a todo. I believe it would be better if some number of 
elevators stayed on the ground floor and others were positioned more smartly, but for now they'll just be waiting
around popular floors, so it's not that bad as well.

For dispatching requests to elevators we calculate two "nice"-like numbers for each elevator
(Unix-inspired, it's obvious that the more favourable things are less nice) and add them up. 
Now that've established that less is more, you can rate my solution very nicely. 
Back to the point: we first calculate a simple distance between the source floor and elevators position.
Then we make sure, that piggybacking is as nice as it can be: 
* elevators waiting or open at the source floor get `-10`
* elevators moving in the direction of source floor get `-1` and an additional `-1` if the direction is the same as request
destination direction
* elevators moving in the opposite direction get a `+1`, which is a bad thing

The importance of direction and distance on final decision can be configured through the `SimpleElevatorStrategy`
constructorparameters. In test scenarios the values are `100` and `1` respectively, cause piggybacking is awesome.

### Build instructions
You guys for sure have `sbt`, so all you have to do is run the tests with:
```
$ sbt test
```
Since the tests pass, nothing can go wrong, right?