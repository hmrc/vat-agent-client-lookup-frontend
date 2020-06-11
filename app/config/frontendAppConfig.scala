/*
 * Copyright 2020 HM Revenue & Customs
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
import config.features.Features
import config.{ConfigKeys => Keys}
import play.api.{Configuration, Environment}
import play.api.i18n.Lang
import play.api.mvc.Call
import uk.gov.hmrc.play.binders.ContinueUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {
  val contactHost: String
  val assetsPrefix: String
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
  val environmentHost: String
  val feedbackUrl: String
  val selfLookup: String
  val emailVerificationBaseUrl: String
  val agentSignUpUrl: String
  val submitVatReturnsUrl: String
  val onlineAgentServicesUrl: String
  val features: Features
  val vatCertificateUrl: String
  val submittedReturnsUrl: String
  val returnDeadlinesUrl: String
  val classicServicesSignInUrl: String
  val accessibilityLinkUrl: String
  val cancelRegistrationUrl: String
  val vat7FormUrl: String
  val optOutMtdVatUrl: String
  val agentServicesHost: String
  val agentServicesUrl: String
  val staticDateValue: String
  val signUpServiceHost: String
  val signUpServiceUrl: String => String
  val manageVatMissingTraderUrl: String
}

@Singleton
class FrontendAppConfig @Inject()(val runModeConfiguration: Configuration, environment: Environment, sc: ServicesConfig) extends AppConfig {

  override lazy val selfLookup: String = sc.baseUrl("selfLookup")

  override val features = new Features(runModeConfiguration)

  override lazy val contactHost: String = sc.getString(Keys.contactFrontendHost)
  private val contactFormServiceIdentifier = "VATC"
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  override lazy val assetsPrefix: String = sc.getString(Keys.assetsUrl) + sc.getString(Keys.assetsVersion)

  override lazy val agentServicesGovUkGuidance: String = sc.getString(Keys.govUkSetupAgentServices)

  private lazy val governmentGatewayHost: String = sc.getString(Keys.governmentGatewayHost)

  private lazy val feedbackSurveyBase = sc.getString(Keys.surveyFrontend) + sc.getString(Keys.surveyContext)
  override lazy val feedbackSurveyUrl = s"$feedbackSurveyBase/VATCA"

  private lazy val signInOrigin: String = sc.getString(Keys.appName)
  private lazy val signInBaseUrl: String = sc.getString(Keys.signInBaseUrl)
  override lazy val signInContinueBaseUrl: String = sc.getString(Keys.signInContinueBaseUrl)
  override lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"
  private lazy val signInContinueUrl: String =
    ContinueUrl(
      signInContinueBaseUrl + controllers.agent.routes.SelectClientVrnController.show(manageVatCustomerDetailsUrl).url
    ).encodedUrl

  override lazy val feedbackSignOutUrl: String = s"$governmentGatewayHost/gg/sign-out?continue=$feedbackSurveyUrl"
  override lazy val unauthorisedSignOutUrl: String = s"$governmentGatewayHost/gg/sign-out?continue=$signInContinueUrl"

  override lazy val classicServicesSignInUrl: String = s"$governmentGatewayHost/gg/sign-out?continue=${sc.getString(Keys.classicServicesSignIn)}"

  override def routeToSwitchLanguage: String => Call = (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)

  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  private def whitelistConfig(key: String): Seq[String] = Some(new String(Base64.getDecoder
    .decode(sc.getString(key)), "UTF-8"))
    .map(_.split(",")).getOrElse(Array.empty).toSeq

  override lazy val whitelistEnabled: Boolean = sc.getBoolean(Keys.whitelistEnabled)
  override lazy val whitelistedIps: Seq[String] = whitelistConfig(Keys.whitelistedIps)
  override lazy val whitelistExcludedPaths: Seq[Call] = whitelistConfig(Keys.whitelistExcludedPaths).map(path => Call("GET", path))
  override lazy val shutterPage: String = sc.getString(Keys.whitelistShutterPage)

  override lazy val vatSubscriptionUrl: String = sc.baseUrl(Keys.vatSubscription)

  private lazy val manageVatBase: String = sc.getString(Keys.manageVatBase)
  override lazy val manageVatCustomerDetailsUrl: String = manageVatBase + sc.getString(Keys.manageVatContext)
  override lazy val timeoutPeriod: Int = sc.getInt(Keys.timeoutPeriod)
  override lazy val timeoutCountdown: Int = sc.getInt(Keys.timeoutCountdown)

  override lazy val environmentHost: String = sc.getString(Keys.environmentHost)

  override lazy val feedbackUrl: String = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier" +
    s"&backUrl=${ContinueUrl(selfLookup + controllers.agent.routes.SelectClientVrnController.show().url).encodedUrl}"

  override lazy val agentSignUpUrl: String = sc.getString(Keys.agentSignUpUrl)
  override lazy val submitVatReturnsUrl: String = sc.getString(Keys.submitVatReturnsUrl)
  override lazy val onlineAgentServicesUrl: String = sc.getString(Keys.onlineAgentServicesUrl)

  override lazy val emailVerificationBaseUrl: String = sc.baseUrl(Keys.emailVerificationBaseUrl)

  override lazy val optOutMtdVatUrl: String = sc.getString(Keys.optOutMtdVatHost) + sc.getString(Keys.optOutMtdVatUrl)

  override lazy val vatCertificateUrl: String = sc.getString(Keys.vatSummaryFrontendHost) + sc.getString(Keys.vatCertificateEndpoint)
  override lazy val submittedReturnsUrl: String = sc.getString(Keys.viewVatReturnsFrontendHost) + sc.getString(Keys.submittedReturnsEndpoint)
  override lazy val returnDeadlinesUrl: String = sc.getString(Keys.viewVatReturnsFrontendHost) + sc.getString(Keys.returnDeadlinesEndpoint)

  override val accessibilityLinkUrl: String = sc.getString(ConfigKeys.vatSummaryFrontendHost) + sc.getString(ConfigKeys.vatSummaryAccessibilityUrl)

  override val cancelRegistrationUrl: String = sc.getString(ConfigKeys.deregisterVatFrontendHost) + sc.getString(ConfigKeys.deregisterVatFrontendUrl)

  override val vat7FormUrl: String = sc.getString(ConfigKeys.vat7FormUrl)

  override val agentServicesHost: String = sc.getString(ConfigKeys.agentServicesHost)
  override val agentServicesUrl: String = agentServicesHost + sc.getString(ConfigKeys.agentServicesUrl)

  override lazy val staticDateValue: String = sc.getString(Keys.staticDateValue)

  override lazy val signUpServiceHost: String = sc.getString(Keys.signUpServiceHost)
  override lazy val signUpServiceUrl: String => String = vatNumber =>  signUpServiceHost + sc.getString(Keys.signUpServiceUrl) + s"$vatNumber"

  override val manageVatMissingTraderUrl: String = manageVatBase + sc.getString(ConfigKeys.manageVatMissingTraderUrl)
}
