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

package assets

import java.time.LocalDate
import assets.BaseTestConstants._
import models.penalties.PenaltiesSummary
import models.{CustomerDetails, HubViewModel}

object HubViewModelTestConstants {

  def hubViewModel(customerDetails: CustomerDetails, penalties: Option[PenaltiesSummary] = None): HubViewModel = HubViewModel(
    customerDetails,
    vrn,
    LocalDate.parse("2019-01-01"),
    Some(LocalDate.parse("2020-01-01")),
    isOverdue = false,
    isError = false,
    payments = 1,
    directDebitSetup = None,
    penaltiesSummary = penalties
  )
}
