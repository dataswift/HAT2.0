import NativePackagerKeys._
import com.typesafe.sbt.SbtNativePackager._

enablePlugins(JavaAppPackaging)

name := """The HAT"""
organization := """org.hatex"""
version := "2.0-SNAPSHOT"

scalaVersion := "2.11.6"

parallelExecution in Test := false

logLevel := Level.Info