/*
 * Copyright 2025 HM Revenue & Customs
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

package mocks.services

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import services.POACheckService
import utils.TestUtil

import java.time.LocalDate

trait MockPoaCheckService extends TestUtil {
  val mockPoaCheckService: POACheckService = mock[POACheckService]

  def mockChangedOnDateWithInLatestVatPeriod(response: Option[LocalDate]): OngoingStubbing[Option[LocalDate]] =
    when(mockPoaCheckService.changedOnDateWithInLatestVatPeriod(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(response)
}
