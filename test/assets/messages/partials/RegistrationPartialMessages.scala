/*
 * Copyright 2021 HM Revenue & Customs
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

package assets.messages.partials

object RegistrationPartialMessages {

  val cancelRegistrationTitle: String = "Cancel VAT registration"
  val cancelRegistrationContent: String =
    "Cancel your client’s VAT registration if you’re closing the business, transferring ownership or do not need to be VAT registered."

  val cancelRegistrationTitleVatGroup: String = "Cancel VAT registration (opens in a new window or tab)"
  val cancelRegistrationContentVatGroup: String =
    "To disband VAT group, you need to cancel the registration using the VAT7 form."

  val pendingRegistrationTitle: String = "Cancel VAT registration"
  val pendingRegistrationContent: String = "The request to cancel your client’s VAT registration is pending."

  val historicDeregTitle: String = "Your client’s VAT registration"
  val historicDeregContent: String  = "Your client’s VAT registration was cancelled on 1 January 2019."
  val historicDeregLink: String  = "VAT online services for agents (opens in new tab)."

  val futureDeregisterTitle: String = "Your client’s VAT registration"
  val futureDeregisterContent: String = "Your client’s VAT registration will be cancelled on 1 January 2020."
  val futureDeregLink: String  = "VAT online services for agents (opens in new tab)."

  val noPartyTypeErrorContent: String = "Sorry, there is a problem with the service. Try again later."
}
