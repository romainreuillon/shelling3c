name := "schelling3c"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.5"

val simpuzzleVersion = "0.2-SNAPSHOT"

libraryDependencies += "fr.iscpif" %% "mgo" % "1.79-SNAPSHOT"

libraryDependencies += "fr.geocites" %% "marius" % simpuzzleVersion

libraryDependencies += "fr.geocites" %% "guguscalibration" % simpuzzleVersion

libraryDependencies += "fr.iscpif" %% "scalabc" % "0.5-SNAPSHOT"

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += "ISC-PIF" at "http://maven.iscpif.fr/public/"

scalariformSettings

