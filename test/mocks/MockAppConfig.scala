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

package mocks

import config.AppConfig
import config.features.Features
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.{Configuration, Mode}

class MockAppConfig(val runModeConfiguration: Configuration, val mode: Mode = Mode.Test) extends AppConfig {

  override val selfLookup = ""

  override val features: Features = new Features(runModeConfiguration)
  override val environmentHost: String = "localhost"
  override val assetsPrefix = ""
  override val reportAProblemPartialUrl = ""
  override val reportAProblemNonJSUrl = ""
  override val agentServicesGovUkGuidance = "guidance/get-an-hmrc-agent-services-account"
  override val submitVatReturnsUrl = "guidance/submit-vat-returns"
  override val feedbackSurveyUrl: String = "/survey"

  override val signInUrl: String = "/sign-in"
  override val signInContinueBaseUrl: String = "/agent-client-lookup-frontend"

  override val feedbackSignOutUrl: String = "/sign-out"
  override val unauthorisedSignOutUrl = "/unauthorised-sign-out"

  override def routeToSwitchLanguage: String => Call = (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)
  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override val vatSubscriptionUrl: String = "/vat-subscription"
  override val manageVatCustomerDetailsUrl: String = "/customer-details"

  override val timeoutPeriod: Int = 1800
  override val timeoutCountdown: Int = 20

  override val feedbackUrl: String = "/feedback"

  override val emailVerificationBaseUrl: String = "mockEmailBaseUrl"
  override val vatCertificateUrl: String = "/certificate"
  override val whatYouOweUrl: String = "/what-you-owe"
  override val paymentHistoryUrl: String = "/payment-history"
  override val onlineAgentServicesUrl: String = "/onlineAgentServices"
  override val penaltiesChangesUrl: String = "/government/collections/vat-penalties-and-interest"
  override val notSignedUpUrl: String = "/making-tax-digital-for-vat-as-an-agent-step-by-step"
  override val submittedReturnsUrl: String = "/submitted-returns"
  override val returnDeadlinesUrl: String = "/vat-through-software/vat-returns/return-deadlines"
  override val annualAccountingUrl: String = "/vat-through-software/annual-accounting"
  override val vatPaymentOnAccountUrl = "/vat-through-software/payments-on-account"

  override val classicServicesSignInUrl: String = "/classic-services-sign-in"

  override val cancelRegistrationUrl: String = "/deregisterUrl"

  override val agentServicesHost = ""
  override val agentServicesUrl = "/agent-service-account"

  override val agentInvitationsFrontendUrl = "/agent-client-relationships/authorisation-request/client-type"

  override val staticDateValue = "2018-05-01"


  override val vat7FormUrl: String = "/publications/vat-application-to-cancel-your-vat-registration-vat7"

  override val manageVatMissingTraderUrl: String = "/vat-through-software/account/missing-trader"

  override val difficultiesPayingUrl: String = "/difficulties-paying-hmrc"

  override val gtmContainer: String = "x"
  override val financialTransactionsBaseUrl: String = "http://myHost:9999"

  override val penaltiesUrl: String => String = (vrn: String) => s"/vat/penalties/summary/$vrn"
  override val penaltiesFrontendUrl: String = "/vat-through-software/representative/test-only/penalties-stub"
  override val urBannerUrl: String = "url-banner-url"
}
