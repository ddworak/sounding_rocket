name := "sounding_rocket"

version := "1.0"

scalaVersion := "2.11.8"

cancelable in Global := true

val Version = new {
  val scalaLogging = "3.5.0"
  val logback = "1.1.7"
  val scalatest = "3.0.0"
}
libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % Version.scalaLogging,
  "ch.qos.logback" % "logback-classic" % Version.logback,
  "org.scalatest" %% "scalatest" % Version.scalatest % "test"
)
    