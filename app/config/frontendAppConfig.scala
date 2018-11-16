/*
 * Copyright 2018 HM Revenue & Customs
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

package config

import java.util.Base64

import javax.inject.{Inject, Singleton}
import config.{ConfigKeys => Keys}
import play.api.{Configuration, Environment}
import play.api.Mode.Mode
import play.api.i18n.Lang
import play.api.mvc.Call
import uk.gov.hmrc.play.binders.ContinueUrl
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig extends ServicesConfig {
  val contactHost: String
  val assetsPrefix: String
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val agentServicesGovUkGuidance: String
  val feedbackSignOutUrl: String
  val feedbackSurveyUrl: String
  val unauthorisedSignOutUrl: String
  val signInUrl: String
  val signInContinueBaseUrl: String
  def routeToSwitchLanguage: String => Call
  def languageMap: Map[String, Lang]
  val whitelistEnabled: Boolean
  val whitelistedIps: Seq[String]
  val whitelistExcludedPaths: Seq[Call]
  val shutterPage: String
  val vatSubscriptionUrl: String
  val manageVatCustomerDetailsUrl: String
  val timeoutPeriod: Int
  val timeoutCountdown: Int
  val agentInvitationsFastTrack: String
  val environmentBase: String
  val feedbackUrl: String
  val selfLookup: String
}

@Singleton
class FrontendAppConfig @Inject()(val runModeConfiguration: Configuration, environment: Environment) extends AppConfig {

  override lazy val selfLookup: String = baseUrl("selfLookup")

  override protected def mode: Mode = environment.mode

  override lazy val contactHost: String = getString(Keys.contactFrontendHost)
  private val contactFormServiceIdentifier = "VATC"
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  override lazy val assetsPrefix: String = getString(Keys.assetsUrl) + getString(Keys.assetsVersion)

  override lazy val analyticsToken: String = getString(Keys.googleAnalyticsToken)
  override lazy val analyticsHost: String = getString(Keys.googleAnalyticsHost)

  override lazy val agentServicesGovUkGuidance: String = getString(Keys.govUkSetupAgentServices)

  private lazy val governmentGatewayHost: String = getString(Keys.governmentGatewayHost)

  private lazy val feedbackSurveyBase = getString(Keys.surveyFrontend) + getString(Keys.surveyContext)
  override lazy val feedbackSurveyUrl = s"$feedbackSurveyBase/?origin=$contactFormServiceIdentifier"

  private lazy val signInOrigin: String = getString(Keys.appName)
  private lazy val signInBaseUrl: String = getString(Keys.signInBaseUrl)
  override lazy val signInContinueBaseUrl: String = getString(Keys.signInContinueBaseUrl)
  override lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"
  private lazy val signInContinueUrl: String =
    ContinueUrl(
      signInContinueBaseUrl + controllers.agent.routes.SelectClientVrnController.show(manageVatCustomerDetailsUrl).url
    ).encodedUrl

  override lazy val feedbackSignOutUrl: String = s"$governmentGatewayHost/gg/sign-out?continue=$feedbackSurveyUrl"
  override lazy val unauthorisedSignOutUrl: String = s"$governmentGatewayHost/gg/sign-out?continue=$signInContinueUrl"

  override def routeToSwitchLanguage: String => Call = (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)
  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  private def whitelistConfig(key: String): Seq[String] = Some(new String(Base64.getDecoder
   .decode(getString(key)), "UTF-8"))
   .map(_.split(",")).getOrElse(Array.empty).toSeq

  override lazy val whitelistEnabled: Boolean = getBoolean(Keys.whitelistEnabled)
  override lazy val whitelistedIps: Seq[String] = whitelistConfig(Keys.whitelistedIps)
  override lazy val whitelistExcludedPaths: Seq[Call] = whitelistConfig(Keys.whitelistExcludedPaths).map(path => Call("GET", path))
  override lazy val shutterPage: String = getString(Keys.whitelistShutterPage)

  override lazy val vatSubscriptionUrl: String = baseUrl(Keys.vatSubscription)

  private lazy val manageVatBase: String = getString(Keys.manageVatBase)
  override lazy val manageVatCustomerDetailsUrl: String = manageVatBase + getString(Keys.manageVatContext)
  override lazy val timeoutPeriod: Int = getInt(Keys.timeoutPeriod)
  override lazy val timeoutCountdown: Int = getInt(Keys.timeoutCountdown)
  override lazy val agentInvitationsFastTrack: String = getString(Keys.agentInvitationsFastTrack)

  override lazy val environmentBase: String = getString(Keys.environmentBase)

  override lazy val feedbackUrl: String = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier" +
    s"&backUrl=${ContinueUrl(selfLookup + controllers.agent.routes.SelectClientVrnController.show().url).encodedUrl}"

}
