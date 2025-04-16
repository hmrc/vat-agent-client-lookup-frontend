/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.*
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.*


val appName = "vat-agent-client-lookup-frontend"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.16"

val bootstrapPlayVersion       = "9.11.0"
val playFrontendHmrc           = "12.0.0"
val mockitoVersion             = "3.2.10.0"
val scalaMockVersion           = "6.0.0"
val domainVersion              = "11.0.0"

val compile = Seq(
  ws,
  "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
  "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % playFrontendHmrc,
  "uk.gov.hmrc"       %% "domain-play-30"             % domainVersion
)

def test(scope: String = "test,it"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc"       %% "bootstrap-test-play-30"       % bootstrapPlayVersion  % Test,
  "org.scalamock"     %% "scalamock"                    % scalaMockVersion      % Test,
  "org.scalatestplus" %% "mockito-3-4"                  % mockitoVersion        % Test
)

lazy val coverageSettings: Seq[Setting[?]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    "views.html.*",
    "app.*",
    "prod.*",
    "config.*",
    "testOnly.*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val appDependencies: Seq[ModuleID] = compile ++ test()

lazy val plugins : Seq[Plugins] = Seq.empty
lazy val playSettings : Seq[Setting[?]] = Seq.empty

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins((Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins) *)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 9149)
  .settings(playSettings *)
  .settings(coverageSettings *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    Test / Keys.fork := true,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    routesGenerator := InjectedRoutesGenerator,
      routesImport := Seq.empty,
        scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Wconf:cat=unused-imports&site=.*views.html.*:s")
  )
/*  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory) (base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false)*/
lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .disablePlugins(SbtGitVersioning)
  .settings(DefaultBuildSettings.itSettings())
Test / javaOptions += "-Dlogger.resource=logback-test.xml"

