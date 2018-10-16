lazy val commonSettings = Seq(
  scalaVersion := "2.12.7",
  organization := "com.emarsys",
  name := "gcs-storage-stream",
  version := "1.1.1"
)

lazy val IntegrationTest = config("it") extend Test
lazy val root = (project in file(".")).
  configs(IntegrationTest).
  settings(commonSettings: _*).
  settings(Defaults.itSettings: _*).
  settings(

libraryDependencies ++= {
val akkaVersion = "2.5.17"
Seq(
  "com.typesafe.akka" %% "akka-stream"                  % akkaVersion,
  "com.typesafe.akka" %% "akka-actor"                   % akkaVersion,
  "org.scalatest"     %% "scalatest"                    % "3.0.1" % "it,test",
  "com.google.cloud"  %   "google-cloud-storage"        % "1.49.0"
 )
}

)

publishTo := Some(Resolver.file("releases", new File("releases")))