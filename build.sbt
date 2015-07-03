import NativePackagerKeys._
import com.typesafe.sbt.SbtNativePackager._

enablePlugins(JavaAppPackaging)

name := """hat-dal"""

version := "2.2-SNAPSHOT"

scalaVersion := "2.11.6"

parallelExecution in Test := false

//logLevel := Level.Debug