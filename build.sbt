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

import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import play.sbt.routes.RoutesKeys
import sbt.Tests.{Group, SubProcess}

val appName = "vat-agent-client-lookup-frontend"

val bootstrapPlayVersion       = "7.12.0"
val playFrontendHmrc           = "5.4.0-play-28"
val jsoupVersion               = "1.15.3"
val mockitoVersion             = "3.1.2.0"
val scalaMockVersion           = "5.2.0"
val domainVersion    = "8.1.0-play-28"
val playJodaVersion            = "2.9.2"

val compile = Seq(
  ws,
  "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
  "uk.gov.hmrc"       %% "play-frontend-hmrc"         % playFrontendHmrc,
  "uk.gov.hmrc"       %% "domain"                     % domainVersion,
  "com.typesafe.play" %% "play-json-joda"             % playJodaVersion
)

def test(scope: String = "test,it"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc"       %% "bootstrap-test-play-28"       % bootstrapPlayVersion  % scope,
  "org.scalamock"     %% "scalamock"                    % scalaMockVersion      % scope,
  "org.jsoup"         % "jsoup"                         % jsoupVersion          % scope,
  "org.scalatestplus" %% "mockito-3-3"                  % mockitoVersion        % scope
)

RoutesKeys.routesImport := Seq.empty

lazy val coverageSettings: Seq[Setting[_]] = {
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
lazy val playSettings : Seq[Setting[_]] = Seq.empty

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = tests map {
  test =>
    Group(test.name, Seq(test), SubProcess(
      ForkOptions().withRunJVMOptions(Vector("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml"))
    ))
}

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins : _*)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 9149)
  .settings(playSettings : _*)
  .settings(coverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    Test / Keys.fork := true,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml",
    scalaVersion := "2.13.8",
    majorVersion := 0,
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    routesGenerator := InjectedRoutesGenerator
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory) (base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false)

