scalaVersion := "2.13.18"
organization := "com.felstar"
name         := "zio-restful-webservice"

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "dev.zio"         %% "zio"            % "2.1.27",
  "dev.zio"         %% "zio-json"       % "1.1.2",
  "dev.zio"         %% "zio-http"       % "3.11.6",
  "dev.zio"         %% "zio-logging"    % "2.5.3",
  "io.getquill"     %% "quill-zio"      % "4.8.5",
  "io.getquill"     %% "quill-jdbc-zio" % "4.8.5",
  "com.h2database"  % "h2"              % "2.5.252",
  "dev.zio"         %% "zio-cache"      % "0.3.0",
  "dev.zio"         %% "zio-actors"     % "0.1.0",
  "io.univalence"   %% "zio-spark"      % "0.12.0",
  "org.apache.spark" %% "spark-core"    % "3.3.2",
  "org.apache.spark" %% "spark-sql"     % "3.3.2",
  "com.github.jwt-scala" %% "jwt-core"  % "12.0.0",
  "com.github.fancellu" % "openai-scala-models" % "v1.1.0.beta3"
)

libraryDependencies ++= Seq(
  "dev.zio"         %% "zio-test"       % "2.1.27",
  "dev.zio"         %% "zio-test-sbt"   % "2.1.27",
  "dev.zio"         %% "zio-http-testkit" % "3.11.6"
).map(_%Test)

scalacOptions ++= Seq(
  "-Ywarn-value-discard",
  "-Xfatal-warnings",
  "-deprecation"
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
// Quill 4.8.5 still asks for zio-json 0.7.x, zio-http 3 needs 1.x
ThisBuild / libraryDependencySchemes += "dev.zio" %% "zio-json" % VersionScheme.Always
