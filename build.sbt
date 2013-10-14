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

libraryDependencies +=
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

libraryDependencies +=
  "org.jsoup" % "jsoup" % "1.7.2"

play.Project.playScalaSettings
