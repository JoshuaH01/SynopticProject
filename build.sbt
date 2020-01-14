import play.sbt.routes.RoutesKeys
import sbt.Resolver

scalaVersion     := "2.13.0"
version          := "0.1.0-SNAPSHOT"
organization     := "com.example"
organizationName := "example"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .settings(
    libraryDependencies   ++= Dependencies.compile ++ Dependencies.test()
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )

name := "SynopticProject"

routesGenerator := InjectedRoutesGenerator

RoutesKeys.routesImport += "models.EmployeeId"
RoutesKeys.routesImport += "models.EmployeePin"