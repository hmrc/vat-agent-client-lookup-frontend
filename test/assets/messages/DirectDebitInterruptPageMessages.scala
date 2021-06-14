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

package assets.messages

object DirectDebitInterruptPageMessages {
  val title: String = "Your client needs to set up a new Direct Debit"
  val para1: String =
    "Your client has been migrated to HMRC’s new online system and their Direct Debit has now been cancelled."
  val para2: String = "To set up a new Direct Debit, your client will need to log in to or create an online " +
    "Business Tax Account. Once your client logs in to their Business Tax Account, instructions will guide " +
    "them through setting up a new Direct Debit for their VAT Bill."
  val checkboxLabel: String = "I will inform my client about this change"
  val buttonText: String = "Continue to your client’s VAT account"
  val formErrorHeading: String = "There is a problem"
  val formErrorText: String = "Select the checkbox to confirm you will inform your client about this change"
  val boldText: String = "your client"
}
