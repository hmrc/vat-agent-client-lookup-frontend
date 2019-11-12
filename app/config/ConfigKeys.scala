/*
 * Copyright 2019 HM Revenue & Customs
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

  val govUkSetupAgentServices: String = "govuk.guidance.setupAgentServices.url"
  val agentSignUpUrl: String = "govuk.guidance.agentSignUp.url"
  val submitVatReturnsUrl: String = "govuk.guidance.submitVatReturns.url"

  val governmentGatewayHost: String = "government-gateway.host"

  val appName: String = "appName"

  val signInBaseUrl: String = "signIn.url"
  val signInContinueBaseUrl: String = "signIn.continueBaseUrl"

  val whitelistEnabled: String = "whitelist.enabled"
  val whitelistedIps: String = "whitelist.allowedIps"
  val whitelistExcludedPaths: String = "whitelist.excludedPaths"
  val whitelistShutterPage: String = "whitelist.shutter-page-url"

  val vatSubscription: String = "vat-subscription"

  val surveyFrontend: String = "feedback-frontend.host"
  val surveyContext: String = "feedback-frontend.endpoints.survey"

  val manageVatBase: String = "manage-vat-subscription-frontend.host"
  val manageVatContext: String = "manage-vat-subscription-frontend.endpoints.customer-details"

  val timeoutPeriod: String = "timeout.period"
  val timeoutCountdown: String = "timeout.countdown"

  val agentInvitationsFastTrack: String = "agent-invitations-fast-track.url"

  val environmentHost: String = "environment-base.host"

  val emailVerificationBaseUrl: String = "email-verification"

  val optOutMtdVatHost:String = "vat-opt-out-frontend.host"
  val optOutMtdVatUrl:String = "vat-opt-out-frontend.url"

  val emailVerificationFeature: String = "features.emailVerification.enabled"
  val preferenceJourneyFeature: String = "features.preferenceJourney.enabled"
  val useLanguageSelectorFeature: String = "features.useLanguageSelector.enabled"
  val useAgentHubPageFeature: String = "features.useAgentHubPage.enabled"

  val vatSummaryFrontendHost: String = "vat-summary-frontend.host"
  val vatCertificateEndpoint: String = "vat-summary-frontend.endpoints.vat-certificate"
  val vatSummaryAccessibilityUrl: String = "vat-summary-frontend.endpoints.accessibilityUrl"

  val viewVatReturnsFrontendHost: String = "view-vat-returns-frontend.host"
  val submittedReturnsEndpoint: String = "view-vat-returns-frontend.endpoints.submitted-returns"
  val returnDeadlinesEndpoint: String = "view-vat-returns-frontend.endpoints.return-deadlines"

  val classicServicesSignIn: String = "classic-services.sign-in-url"

  val deregisterVatFrontendHost: String = "deregister-vat-frontend.host"
  val deregisterVatFrontendUrl: String = "deregister-vat-frontend.endpoints.deregister"
}
