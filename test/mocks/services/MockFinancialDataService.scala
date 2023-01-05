/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{Charge, DirectDebit}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockFinancialDataService extends TestUtil {

  val mockFinancialDataService: FinancialDataService = mock[FinancialDataService]

  def mockDirectDebitResponse(response: HttpResult[DirectDebit]): OngoingStubbing[Future[HttpResult[DirectDebit]]] =
    when(mockFinancialDataService.getDirectDebit(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(Future.successful(response))

  def mockPaymentResponse(response: HttpResult[Seq[Charge]]): OngoingStubbing[Future[HttpResult[Seq[Charge]]]] =
    when(mockFinancialDataService.getPayment(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext]))
      .thenReturn(Future.successful(response))
}
