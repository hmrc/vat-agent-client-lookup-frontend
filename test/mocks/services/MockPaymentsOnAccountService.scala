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

import assets.BaseTestConstants._
import models.StandingRequest
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import services.PaymentsOnAccountService

import scala.concurrent.Future

trait MockPaymentsOnAccountService extends AnyWordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach {

  val mockPaymentsOnAccountService: PaymentsOnAccountService = mock[PaymentsOnAccountService]

  type StandingRequestResponse = Option[StandingRequest]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockPaymentsOnAccountService)
  }

  def setupMockStandingRequest(vrn: String)(response: StandingRequestResponse): OngoingStubbing[Future[StandingRequestResponse]] = {
    when(mockPaymentsOnAccountService.getPaymentsOnAccounts(ArgumentMatchers.eq(vrn))
    (ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(response))
  }

  def mockStandingRequest(standingRequest: Option[StandingRequest]): OngoingStubbing[Future[StandingRequestResponse]] =
    setupMockStandingRequest(vrn)((standingRequest))

}
