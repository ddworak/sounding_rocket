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
lorem ipsum
```scala
trait ElevatorControlSystem {
  def status(): Seq[(Int, Int, Int)]
  def update(Int, Int, Int): Unit
  def pickup(Int, Int): Unit
  def step(): Unit
}
```

### The Solution
lorem ipsum 

### Build instructions
```
$ sbt sth
```