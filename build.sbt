

organization := "com.sandinh"
name := "paho-akka"

version := "1.6.4-inspired"

scalaVersion := "2.12.5"


credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-target:jvm-1.8")
scalacOptions ++= (CrossVersion.scalaApiVersion(scalaVersion.value) match {
  case Some((2, 11)) => Seq("-Ybackend:GenBCode")
  case _ => Nil
})

publishTo := Some("Artifactory Realm" at "https://artifacts.dev.inspired.ag/artifactory/inspired-releases/")
resolvers ++= Seq(
  "DefaultMavenRepository" at "https://repo1.maven.org/maven2/",
  "JavaNet1Repository" at "http://download.java.net/maven/1/",
  Resolver.sonatypeRepo("public"),
  Resolver.typesafeRepo("releases"),
  "inspired snapshots" at "https://artifacts.dev.inspired.ag/artifactory/inspired-snapshots/",
  "inspired releases" at "https://artifacts.dev.inspired.ag/artifactory/inspired-releases/"
)
resolvers += "Paho Releases" at "https://repo.eclipse.org/content/repositories/paho-releases"

val pahoLibName = "org.eclipse.paho.client.mqttv3"
def pahoSnapshotUrl() = {
  import com.softwaremill.sttp._, okhttp._
  val v = System.getenv("PAHO_CLIENT_VERSION")
  val urlBase = s"https://repo.eclipse.org/content/repositories/paho-snapshots/org/eclipse/paho/$pahoLibName/$v"
  implicit val handler = OkHttpSyncHandler()
  val res = sttp.get(uri"$urlBase/maven-metadata.xml").send().body.getOrElse(null)
  val meta = scala.xml.XML.loadString(res)
  val jarNode = meta \ "versioning" \ "snapshotVersions" \ "snapshotVersion" filter { n =>
    (n \ "extension").text == "jar" && (n \ "classifier").isEmpty
  }
  val fileName = (jarNode \ "value").text
  s"$urlBase/$pahoLibName-$fileName.jar"
}
//for test against multiple paho client versions
//see https://github.com/eclipse/paho.mqtt.java/issues/405
lazy val pahoLib = {
  val lib = "org.eclipse.paho" % pahoLibName
  System.getenv("PAHO_CLIENT_VERSION") match {
    case null => lib % "1.1.1"
    case v if !v.endsWith("-SNAPSHOT") => lib % v
    case v => lib % v from pahoSnapshotUrl()
  }
}

libraryDependencies ++= Seq(
  pahoLib,
  "com.typesafe.akka" %% "akka-actor"     % "2.5.11",
  "org.log4s"         %% "log4s"          % "1.3.6",
  "org.scalatest"     %% "scalatest"      % "3.0.4"   % Test,
  "com.typesafe.akka" %% "akka-testkit"   % "2.5.11"   % Test,
  "ch.qos.logback"    % "logback-classic" % "1.2.3"   % Test
)
