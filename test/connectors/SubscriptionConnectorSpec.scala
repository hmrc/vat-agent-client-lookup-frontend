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

package connectors

import assets.BaseTestConstants._
import assets.CustomerDetailsTestConstants._
import connectors.httpParsers.ResponseHttpParser.HttpResult
import mocks.MockHttp
import models.CustomerDetails
import play.api.http.Status
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtil

import scala.concurrent.Future

class SubscriptionConnectorSpec extends TestUtil with MockHttp {

  val errorResponse: HttpResponse = HttpResponse(Status.BAD_REQUEST, "Error Message")

  object TestSubscriptionConnector extends SubscriptionConnector(mockHttp,mockConfig)

  "SubscriptionConnector" when {

    "calling .getCustomerDetailsUrl" should {

      "format the url correctly" in {
        val testUrl = TestSubscriptionConnector.getCustomerDetailsUrl(vrn)
        testUrl shouldBe s"${mockConfig.vatSubscriptionUrl}/vat-subscription/$vrn/full-information"
      }
    }

    "calling .getCustomerDetails" when {

      def result: Future[HttpResult[CustomerDetails]] = TestSubscriptionConnector.getCustomerDetails(vrn)

      "called for a Right with CustomerDetails" should {

        "return a CustomerDetailsModel" in {
          setupMockHttpGet(TestSubscriptionConnector.getCustomerDetailsUrl(vrn))(Right(customerDetailsOrganisation))
          await(result) shouldBe Right(customerDetailsOrganisation)
        }
      }

      "given an error should" should {

        "return a Left with an error response" in {
          setupMockHttpGet(TestSubscriptionConnector.getCustomerDetailsUrl(vrn))(Left(errorResponse))
          await(result) shouldBe Left(errorResponse)
        }
      }
    }
  }
}
