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

package mocks.services

import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.penalties.PenaltiesSummary
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import services.PenaltiesService
import utils.TestUtil

import scala.concurrent.Future

trait MockPenaltiesService extends TestUtil {
  val mockPenaltiesService: PenaltiesService = mock[PenaltiesService]

  def mockPenaltiesResponse(response: Option[HttpResult[PenaltiesSummary]]): OngoingStubbing[Future[Option[HttpResult[PenaltiesSummary]]]] =
    when(mockPenaltiesService.getPenaltiesInformation(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))
}
