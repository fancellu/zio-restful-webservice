scalaVersion := "2.13.8"
organization := "com.felstar"
name         := "zio-restful-webservice"

libraryDependencies ++= Seq(
  "dev.zio"         %% "zio"            % "2.0.3",
  "dev.zio"         %% "zio-json"       % "0.3.0",
  "dev.zio"         %% "zio-http"       % "0.0.3",
  "io.getquill"     %% "quill-zio"      % "4.6.0",
  "io.getquill"     %% "quill-jdbc-zio" % "4.6.0",
  "com.h2database"  % "h2"              % "2.1.214",
  "dev.zio"         %% "zio-cache"      % "0.2.0",
  "dev.zio"         %% "zio-actors"     % "0.1.0",
  "io.univalence"   %% "zio-spark"      % "0.8.1",
  "org.apache.spark" %% "spark-core"    % "3.3.0",
  "org.apache.spark" %% "spark-sql"     % "3.3.0"
)

libraryDependencies ++= Seq(
  "dev.zio"         %% "zio-test"       % "2.0.3",
  "dev.zio"         %% "zio-test-sbt"   % "2.0.3",
).map(_%Test)

scalacOptions ++= Seq(
  "-Ywarn-value-discard",
  "-Xfatal-warnings",
  "-deprecation"
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)