import play.core.PlayVersion
import sbt._
import play.sbt.PlayImport._

object Dependencies {

  val compile = Seq(
                    ws
                    )

  def test(scope: String = "test") = Seq(
                       "org.scalatest" %% "scalatest" % "3.0.8",
                       "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope,
                       "org.reactivemongo" %% "play2-reactivemongo" % "0.18.1-play27",
                       "org.scalactic" %% "scalactic" % "3.0.8",
                       "org.scalatest" %% "scalatest" % "3.0.8" % scope,
                       "org.mockito" % "mockito-all" % "1.10.19" % scope,
                       "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope,
                       "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
                        )

}

