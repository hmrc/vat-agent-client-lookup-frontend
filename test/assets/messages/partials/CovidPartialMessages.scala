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

package assets.messages.partials

object CovidPartialMessages {

  val headingPreEnd: String = "We previously set out that you could delay (defer) paying VAT because of coronavirus " +
    "(COVID-19). The VAT deferral period ends on 30 June 2020."
  val headingPostEnd: String = "We previously set out that you could delay (defer) paying VAT because of coronavirus " +
    "(COVID-19). The VAT deferral period ended on 30 June 2020."
  val line1: String = "VAT bills with a payment date on or after 1 July 2020 must be paid on time and in full."
  val line2link: String = "Payment Support Service"
  val line2: String = s"If you cancelled your Direct Debit, set it up again so you do not miss a payment. Contact our $line2link " +
    "as soon as possible if you cannot pay. You might be able to set up a Time to Pay agreement if you’re struggling to pay a tax bill."
  val line3: String = "You still need to submit VAT Returns, even if your business has temporarily closed."
  val line4: String = "You have until 31 March 2021 to pay VAT bills that were due between 20 March 2020 and 30 June 2020."
}
