name := "rsshub"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

//libraryDependencies += "rome" % "rome" % "1.0"

play.Project.playScalaSettings
