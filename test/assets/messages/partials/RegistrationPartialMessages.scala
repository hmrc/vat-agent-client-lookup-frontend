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

package assets.messages.partials

import assets.CustomerDetailsTestConstants._


object RegistrationPartialMessages {

  val cancelRegistrationTitle: String = "Cancel VAT registration"
  val cancelRegistrationContent: String =
    "Cancel your client’s VAT registration if you’re closing the business, transferring ownership or do not need to be VAT registered."

  val pendingRegistrationTitle: String = "Cancel VAT registration"
  val pendingRegistrationContent: String = "The request to cancel your client's VAT registration is pending."

  val historicDeregTitle: String = "Your client’s VAT registration"
  val historicDeregContent: String  = s"Your client’s VAT registration was cancelled on 1 January 2019."
  val historicDeregLink: String  = "VAT online services for agents (opens in new tab)."
}
