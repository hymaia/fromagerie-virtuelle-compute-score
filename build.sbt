name := "fromagerie-virtuelle-compute-score"

version := "0.1"

scalaVersion := "2.12.10"

idePackagePrefix := Some("fr.hymaia.fromagerie")

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.3.1" % Provided
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.3.1" % Provided
libraryDependencies += "com.amazonaws" % "aws-java-sdk-glue" % "1.12.384" % Provided
libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.12.384" % Provided

excludeFilter in unmanagedSources := "Runner.scala"
