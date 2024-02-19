val Http4sVersion          = "0.23.23"
val CirceVersion           = "0.14.5"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.4.11"
val MunitCatsEffectVersion = "1.0.7"
val distDir                = "dist/"

lazy val makeDistribution = taskKey[Unit]("Move distributable JAR to another directory")

lazy val root = (project in file("."))
  .settings(
    organization                     := "fi.johannes",
    name                             := "loadbalancer",
    scalaVersion                     := "3.3.1",
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"            %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
      "org.scalameta"         %% "munit"               % MunitVersion           % Test,
      "org.typelevel"         %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback"         % "logback-classic"     % LogbackVersion         % Runtime,
      "com.github.pureconfig" %% "pureconfig-core"     % "0.17.4",
    ),
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x                   => (assembly / assemblyMergeStrategy).value.apply(x)
    },
    assembly / mainClass             := Some("Main"),
    assembly / assemblyJarName       := "loadbalancer.jar",

    /**
      * Create fat jar and move to dist/
      */
    makeDistribution                          := {
      val assemblyTask = assembly.value

      val sourceJar      = (assembly / assemblyOutputPath).value
      val destinationDir = file(distDir) // Replace with your actual destination directory

      // Check if the destination directory exists, and create it if not
      if (!destinationDir.exists()) {
        destinationDir.mkdirs()
        println(s"distDir created: $destinationDir")
      }

      // Move the JAR using java.nio.file.Files
      import java.nio.file._
      Files.copy(
        sourceJar.toPath,
        destinationDir.toPath.resolve(sourceJar.getName),
        StandardCopyOption.REPLACE_EXISTING,
      )

      // Print a message indicating the move is successful
      println(s"Fat JAR moved to: ${destinationDir.toPath.resolve(sourceJar.getName)}")
    },
  )
