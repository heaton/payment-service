import scala.util.Properties.envOrElse

val catsEffectVersion = "2.2.0"
val Http4sVersion = "0.21.8"
val CirceVersion = "0.13.0"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    organization := "me.heaton",
    name := "payment-service",
    version := envOrElse("APP_VERSION", "0.1.0-SNAPSHOT"),
    scalaVersion := "2.12.11",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-finagle" % s"$Http4sVersion-20.9.0",
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-literal" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.scalatest" %% "scalatest" % "3.2.1" % Test,
      "org.scalatestplus" %% "mockito-3-4" % "3.2.1.0" % Test,
      "org.typelevel" %% "cats-effect-laws" % catsEffectVersion % Test,
      "org.mockito" % "mockito-core" % "3.4.6" % Test
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4")
  )

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings"
)

coverageMinimum in ThisBuild := 100
coverageFailOnMinimum in ThisBuild := true
coverageExcludedPackages in root := "me.heaton.payments.Main"
