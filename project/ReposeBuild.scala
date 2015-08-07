import sbt._
import Keys._


object Deps {
  //TODO: these can be in a separate file
  val jaxb2Runtime = "org.jvnet.jaxb2_commons" % "jaxb2-basics-runtime" % "0.6.4"
  val guava = "com.google.guava" % "guava" % "14.0.1"
  val httpclient = "org.apache.httpcomponents" % "httpclient" % "4.4.1"

  val powermockVersion = "1.5.4"
  val log4jVersion = "2.3"
  val commonDeps = Seq(
    "org.slf4j" % "slf4j-api" % "1.7.7",
    "org.codehaus.groovy" % "groovy-all" % "2.1.3" % "test",
    "org.spockframework" % "spock-core" % "0.7-groovy-2.0" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "junit" % "junit" % "4.12" % "test",
    "com.novocode" % "junit-interface" % "0.11" % Test,
    "org.powermock" % "powermock-module-junit4" % powermockVersion % "test",
    "org.powermock" % "powermock-api-mockito" % powermockVersion % "test",
    "org.hamcrest" % "hamcrest-all" % "1.3" % "test",
    "org.glassfish" % "javax.servlet" % "3.1",
    "javax" % "javaee-web-api" % "6.0" % Runtime,
    "javax.transaction" % "transaction-api" % "1.1" % Runtime,
    "org.apache.logging.log4j" % "log4j-core" % log4jVersion % "test",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion % "test",
    "org.apache.logging.log4j" % "log4j-api" % log4jVersion % "test",
    "org.apache.logging.log4j" % "log4j-core" % log4jVersion % "test" classifier "tests"
  )
}

//Project(..., settings = Project.defaultSettings ++ org.softnetwork.sbt.plugins.GroovyPlugin.groovy.settings ++ org.softnetwork.sbt.plugins.GroovyPlugin.testGroovy.settings)

object ReposeBuild extends Build {

  import Deps._

  val groovySettings = org.softnetwork.sbt.plugins.GroovyPlugin.groovy.settings
  val testGroovySettings = org.softnetwork.sbt.plugins.GroovyPlugin.testGroovy.settings

  val baseDir = "./repose-aggregator"

  //JAXB only project!
  lazy val jee6Schemas = Project(
    "jee6-schemas",
    file(s"$baseDir/external/jee6-schemas")
  ).settings()

  lazy val utilities = Project(
    "utilities",
    file(s"$baseDir/commons/utilities")
  ).dependsOn(httpclientApi).
    settings(groovySettings: _*).
    settings(testGroovySettings: _*).
    settings(
      scalaVersion := "2.10.5",
      dependencyOverrides += "org.codehaus.groovy" % "groovy-all" % "2.1.3",
      libraryDependencies ++= commonDeps ++ Seq(
        httpclient,
        jaxb2Runtime,
        "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1",
        "commons-pool" % "commons-pool" % "1.6",
        "org.apache.commons" % "commons-lang3" % "3.3.2",
        "org.scalatest" %% "scalatest" % "2.2.0" % "test",
        "com.mockrunner" % "mockrunner-servlet" % "1.0.0" % "test"
      )
    )

  lazy val httpclientApi = Project(
    "httpclient-api",
    file(s"$baseDir/services/httpclient/api")
  ).settings(
    libraryDependencies ++= commonDeps ++ Seq(
      httpclient
    )
  )

  lazy val coreServiceApi = Project("core-service-api",
    file(s"$baseDir/core/core-service-api")).
    dependsOn(utilities).settings(
    libraryDependencies ++= commonDeps ++ Seq(
      jaxb2Runtime,
      guava
    )
  )

  lazy val coreLib = Project(
    "core-lib",
    file("./repose-aggregator/core/core-lib")).
    dependsOn(jee6Schemas).
    settings(
      libraryDependencies ++= commonDeps ++ Seq(
        //depends on other projects!
        "javax.inject" % "javax.inject" % "1"
      )
    )
}