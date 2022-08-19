scalaVersion := "2.13.8"
organization := "com.felstar"
name         := "zio-restful-webservice"

libraryDependencies ++= Seq(
  "dev.zio"         %% "zio"            % "2.0.0",
  "dev.zio"         %% "zio-json"       % "0.3.0-RC10",
  "io.d11"          %% "zhttp"          % "2.0.0-RC10",
  "io.getquill"     %% "quill-zio"      % "4.2.0",
  "io.getquill"     %% "quill-jdbc-zio" % "4.2.0",
  "com.h2database"  % "h2"              % "2.1.214"
)

libraryDependencies ++= Seq(
  "dev.zio"         %% "zio-test"       % "2.0.0",
  "dev.zio"         %% "zio-test-sbt"   % "2.0.0",
).map(_%Test)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)