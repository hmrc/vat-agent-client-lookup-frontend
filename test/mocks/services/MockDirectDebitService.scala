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

import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import services.FinancialDataService
import utils.TestUtil
import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.DirectDebit
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockDirectDebitService extends TestUtil {

  val mockDirectDebitService: FinancialDataService = mock[FinancialDataService]

  def mockDirectDebitResponse(response: HttpResult[DirectDebit]): OngoingStubbing[Future[HttpResult[DirectDebit]]] =
    when(mockDirectDebitService.getDirectDebit(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(Future.successful(response))

}
