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

package assets

import java.time.LocalDate

import assets.BaseTestConstants._
import models.{CustomerDetails, HubViewModel}

object HubViewModelTestConstants {

  def hubViewModel(customerDetails: CustomerDetails, hasAnyPenalties: Boolean = false): HubViewModel = HubViewModel(
    customerDetails,
    vrn,
    LocalDate.parse("2019-01-01"),
    showBlueBox = false,
    Some(LocalDate.parse("2020-01-01")),
    isOverdue = false,
    payments = 1,
    directDebitSetup = None,
    shouldShowPenaltiesTile = hasAnyPenalties
  )

  def hubViewModelBlueBox(customerDetails: CustomerDetails): HubViewModel =
    hubViewModel(customerDetails).copy(showBlueBox = true)

}
