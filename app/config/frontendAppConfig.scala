/*
 * Copyright 2024 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import config.features.Features
import config.{ConfigKeys => Keys}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call
import java.net.URLEncoder
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {
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
  val vatSubscriptionUrl: String
  val manageVatCustomerDetailsUrl: String
  val timeoutPeriod: Int
  val timeoutCountdown: Int
  val environmentHost: String
  val feedbackUrl: String
  val selfLookup: String
  val emailVerificationBaseUrl: String
  val submitVatReturnsUrl: String
  val onlineAgentServicesUrl: String
  val penaltiesChangesUrl: String
  val notSignedUpUrl: String
  val features: Features
  val vatCertificateUrl: String
  val vatPaymentOnAccountUrl: String
  val penaltiesFrontendUrl: String
  val whatYouOweUrl: String
  val paymentHistoryUrl: String
  val submittedReturnsUrl: String
  val returnDeadlinesUrl: String
  val classicServicesSignInUrl: String
  val cancelRegistrationUrl: String
  val vat7FormUrl: String
  val agentServicesHost: String
  val agentServicesUrl: String
  val staticDateValue: String
  val manageVatMissingTraderUrl: String
  val difficultiesPayingUrl: String
  val gtmContainer: String
  val financialTransactionsBaseUrl: String
  val penaltiesUrl: String => String
  val agentInvitationsFrontendUrl: String
  val urBannerUrl: String
}

@Singleton
class FrontendAppConfig @Inject()(val runModeConfiguration: Configuration, sc: ServicesConfig) extends AppConfig {

  override lazy val selfLookup: String = sc.baseUrl("selfLookup")

  override val features = new Features(runModeConfiguration)

  private lazy val contactHost: String = sc.getString(Keys.contactFrontendHost)
  private lazy val contactFormServiceIdentifier: String = sc.getString(Keys.contactFrontendIdentifier)

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
    URLEncoder.encode(
      signInContinueBaseUrl + controllers.agent.routes.SelectClientVrnController.show(manageVatCustomerDetailsUrl).url,
      "UTF-8"
    )

  override lazy val feedbackSignOutUrl: String = s"$governmentGatewayHost/bas-gateway/sign-out-without-state?continue=$feedbackSurveyUrl"
  override lazy val unauthorisedSignOutUrl: String = s"$governmentGatewayHost/bas-gateway/sign-out-without-state?continue=$signInContinueUrl"

  override lazy val classicServicesSignInUrl: String =
    s"$governmentGatewayHost/bas-gateway/sign-out-without-state?continue=${sc.getString(Keys.classicServicesSignIn)}"

  override def routeToSwitchLanguage: String => Call = (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)

  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override lazy val vatSubscriptionUrl: String = sc.baseUrl(Keys.vatSubscription)

  private lazy val manageVatBase: String = sc.getString(Keys.manageVatBase)
  override lazy val manageVatCustomerDetailsUrl: String = manageVatBase + sc.getString(Keys.manageVatContext)
  override lazy val timeoutPeriod: Int = sc.getInt(Keys.timeoutPeriod)
  override lazy val timeoutCountdown: Int = sc.getInt(Keys.timeoutCountdown)

  override lazy val environmentHost: String = sc.getString(Keys.environmentHost)

  override lazy val feedbackUrl: String = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier" +
    s"&backUrl=${URLEncoder.encode(selfLookup + controllers.agent.routes.SelectClientVrnController.show().url, "UTF-8")}"

  override lazy val submitVatReturnsUrl: String = sc.getString(Keys.submitVatReturnsUrl)
  override lazy val onlineAgentServicesUrl: String = sc.getString(Keys.onlineAgentServicesUrl)
  override lazy val penaltiesChangesUrl: String = sc.getString(Keys.penaltiesChangesUrl)
  override lazy val notSignedUpUrl: String = sc.getString(Keys.notSignedUpUrl)

  override lazy val emailVerificationBaseUrl: String = sc.baseUrl(Keys.emailVerificationBaseUrl)

  override lazy val vatCertificateUrl: String = sc.getString(Keys.vatSummaryFrontendHost) + sc.getString(Keys.vatCertificateEndpoint)
  override lazy val penaltiesFrontendUrl: String = sc.getString(Keys.penaltiesFrontendHost) + sc.getString(Keys.penaltiesFrontendUrl)
  override lazy val whatYouOweUrl: String = sc.getString(Keys.vatSummaryFrontendHost) + sc.getString(Keys.vatWhatYouOweEndpoint)
  override lazy val paymentHistoryUrl: String = sc.getString(Keys.vatSummaryFrontendHost) + sc.getString(Keys.paymentHistoryEndpoint)
  override lazy val submittedReturnsUrl: String = sc.getString(Keys.viewVatReturnsFrontendHost) + sc.getString(Keys.submittedReturnsEndpoint)
  override lazy val returnDeadlinesUrl: String = sc.getString(Keys.viewVatReturnsFrontendHost) + sc.getString(Keys.returnDeadlinesEndpoint)

  override val cancelRegistrationUrl: String = sc.getString(ConfigKeys.deregisterVatFrontendHost) + sc.getString(ConfigKeys.deregisterVatFrontendUrl)

  override lazy val vatPaymentOnAccountUrl: String = sc.getString(Keys.vatSummaryFrontendHost) + sc.getString(Keys.vatPaymentOnAccountEndpoint)

  override val vat7FormUrl: String = sc.getString(ConfigKeys.vat7FormUrl)

  override val agentServicesHost: String = sc.getString(ConfigKeys.agentServicesHost)
  override val agentServicesUrl: String = agentServicesHost + sc.getString(ConfigKeys.agentServicesUrl)

  override val agentInvitationsFrontendUrl: String = sc.getString(ConfigKeys.agentInvitationsFrontendHost) +
    sc.getString(ConfigKeys.agentInvitationsFrontendUrl)

  override lazy val staticDateValue: String = sc.getString(Keys.staticDateValue)

  override val manageVatMissingTraderUrl: String = manageVatBase + sc.getString(ConfigKeys.manageVatMissingTraderUrl)

  override val difficultiesPayingUrl: String = sc.getString(Keys.difficultiesPayingUrl)

  override val gtmContainer: String = sc.getString(Keys.gtmContainer)

  override val financialTransactionsBaseUrl: String = sc.baseUrl(Keys.financialTransactions)

  override lazy val penaltiesUrl: String => String = vrn => sc.getString(Keys.penaltiesHost) + sc.getString(Keys.penaltiesUrl) + vrn

  override val urBannerUrl: String = sc.getString(Keys.urBannerUrl)

}
