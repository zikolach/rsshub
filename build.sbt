name := "rsshub"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

libraryDependencies +=
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0"

libraryDependencies +=
  "rome" % "rome" % "1.0"

play.Project.playScalaSettings
