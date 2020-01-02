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

package assets.messages

object AgentHubMessages {

  val title = "Your client’s VAT account - Your client’s VAT details - GOV.UK"
  val heading = "Your client’s VAT account"
  val vatNo: String => String = vrn => s"VAT registration number (VRN): $vrn"
  val changeClient = "Change client"
  val agentServicesAccount = "Agent Services Account"
  val clientDetails = "Client details"
  val vatReturns = "VAT Returns"
  val vatCertificate = "VAT certificate"
  val optOut = "Opt out of Making Tax Digital for VAT"
  val cancelVat = "Cancel VAT registration"
}
