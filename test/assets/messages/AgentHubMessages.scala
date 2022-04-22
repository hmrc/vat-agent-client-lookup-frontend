/*
 * Copyright 2022 HM Revenue & Customs
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

package assets.messages

object AgentHubMessages {

  val heading = "Your client’s VAT details"
  val title = s"$heading - Your client’s VAT details - GOV.UK"
  val vatNo: String => String = vrn => s"VAT registration number: $vrn"
  val changeClient = "Change client"
  val agentServicesAccount = "Agent Services Account"
  val clientDetails = "Client details"
  val vatReturns = "VAT Returns"
  val vatCertificate = "VAT certificate"
  val cancelVat = "Cancel VAT registration"
  val manageVat = "Manage VAT"
  val noDDclient = "Agents cannot access a client’s Direct Debit or payment details."
  val notificationBannerTitle = "Important"
  val notificationBannerP1 = "HMRC has had to cancel some VAT Direct Debit agreements."
  val notificationBannerP2 = "If your client pays by Direct Debit, ask them to check that their mandate is still in place."
  val notificationBannerP3 = "If they have already done this they do not need to do it again."
}
