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

package config

object ConfigKeys {
  val assetsUrl: String = "assets.url"
  val assetsVersion: String = "assets.version"

  val contactFrontendHost: String = "contact-frontend.host"
  val contactFrontendIdentifier: String = "contact-frontend.serviceId"

  val govUkSetupAgentServices: String = "govuk.guidance.setupAgentServices.url"
  val submitVatReturnsUrl: String = "govuk.guidance.submitVatReturns.url"
  val onlineAgentServicesUrl: String = "govuk.guidance.onlineAgentServices.url"
  val difficultiesPayingUrl: String = "govuk.guidance.difficultiesPaying.url"

  val governmentGatewayHost: String = "government-gateway.host"

  val appName: String = "appName"

  val signInBaseUrl: String = "signIn.url"
  val signInContinueBaseUrl: String = "signIn.continueBaseUrl"

  val vatSubscription: String = "vat-subscription"

  val surveyFrontend: String = "feedback-frontend.host"
  val surveyContext: String = "feedback-frontend.endpoints.survey"

  val manageVatBase: String = "manage-vat-subscription-frontend.host"
  val manageVatContext: String = "manage-vat-subscription-frontend.endpoints.customer-details"
  val manageVatMissingTraderUrl: String = "manage-vat-subscription-frontend.endpoints.missing-trader"

  val timeoutPeriod: String = "timeout.period"
  val timeoutCountdown: String = "timeout.countdown"

  val environmentHost: String = "environment-base.host"

  val emailVerificationBaseUrl: String = "email-verification"

  val emailVerificationFeature: String = "features.emailVerification.enabled"
  val useStaticDateFeature: String = "features.useStaticDate.enabled"

  val staticDateValue: String = "date-service.staticDate.value"

  val vatSummaryFrontendHost: String = "vat-summary-frontend.host"
  val vatWhatYouOweEndpoint: String = "vat-summary-frontend.endpoints.what-you-owe"
  val paymentHistoryEndpoint: String = "vat-summary-frontend.endpoints.payment-history"
  val vatCertificateEndpoint: String = "vat-summary-frontend.endpoints.vat-certificate"

  val viewVatReturnsFrontendHost: String = "view-vat-returns-frontend.host"
  val submittedReturnsEndpoint: String = "view-vat-returns-frontend.endpoints.submitted-returns"
  val returnDeadlinesEndpoint: String = "view-vat-returns-frontend.endpoints.return-deadlines"

  val classicServicesSignIn: String = "classic-services.sign-in-url"

  val deregisterVatFrontendHost: String = "deregister-vat-frontend.host"
  val deregisterVatFrontendUrl: String = "deregister-vat-frontend.endpoints.deregister"

  val vat7FormUrl: String = "external.vat7Form.url"

  val agentServicesHost: String = "agent-services.host"
  val agentServicesUrl: String = "agent-services.url"

  val agentInvitationsFrontendHost: String = "agent-invitations-frontend.host"
  val agentInvitationsFrontendUrl: String = "agent-invitations-frontend.url"

  val gtmContainer: String = "tracking-consent-frontend.gtm.container"

  val financialTransactions: String = "financial-transactions"

  val penaltiesServiceFeature: String = "features.penaltiesService.enabled"
  val penaltiesHost: String = "penalties.host"
  val penaltiesUrl: String = "penalties.url"
  val penaltiesFrontendHost: String = "penalties-frontend.host"
  val penaltiesFrontendUrl: String = "penalties-frontend.endpoints.home"
}
