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

package mocks.connectors

import connectors.SubscriptionConnector
import models.CustomerDetails
import models.errors.UnexpectedError
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future

trait MockSubscriptionConnector extends AnyWordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach {

  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSubscriptionConnector)
  }

  def setupMockUserDetails(vrn: String)(response: Either[UnexpectedError, CustomerDetails]): Unit = {
    when(mockSubscriptionConnector.getCustomerDetails(ArgumentMatchers.eq(vrn))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))
  }
}
