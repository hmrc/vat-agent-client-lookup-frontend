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

package connectors

import connectors.httpParsers.ResponseHttpParser.HttpResult
import helpers.IntegrationBaseSpec
import models.errors.ServerSideError
import models.{RequestItem, StandingRequest, StandingRequestDetail}
import models.penalties.PenaltiesSummary
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.test.Helpers._
import stubs.{PenaltiesStub, VatSubscriptionStub}
import uk.gov.hmrc.http.HeaderCarrier

class StandingRequestsConnectorISpec extends IntegrationBaseSpec {

  private trait Test {
    val connector: StandingRequestsConnector = app.injector.instanceOf[StandingRequestsConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }
  val jsonStandingRequestScheduleMinimumValid: JsValue = Json.parse(
    """
      |{
      |  "processingDate": "2024-07-15T09:30:47Z",
      |  "standingRequests": [
      |    {
      |      "requestNumber": "20000037272",
      |      "requestCategory": "3",
      |      "createdOn": "2023-11-30",
      |      "requestItems": [
      |        {
      |          "period": "1",
      |          "periodKey": "25A1",
      |          "startDate": "2025-04-01",
      |          "endDate": "2025-06-30",
      |          "dueDate": "2025-06-30",
      |          "amount": 22945.23
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  val modelStandingRequestScheduleMinimumValid: StandingRequest = StandingRequest(
    ("2024-07-15T09:30:47Z"), List(
      StandingRequestDetail(
        requestNumber = "20000037272",
        requestCategory = "3",
        createdOn = ("2023-11-30"),
        changedOn = None,
        requestItems = List(
          RequestItem(
            period = "1",
            periodKey = "25A1",
            startDate = ("2025-04-01"),
            endDate = ("2025-06-30"),
            dueDate = ("2025-06-30"),
            amount = 22945.23,
            chargeReference = None,
            postingDueDate = None
          )
        )
      )
    )
  )

  val jsonSrWithInvalidDate: JsValue = Json.parse(
    """
      |{
      |  "processingDate": "2024111-017-15T09:30:47Z",
      |  "standingRequests": [
      |    {
      |      "requestNumber": "20000037272",
      |      "requestCategory": "3",
      |      "createdOn": "202113-11-30",
      |      "requestItems": [
      |        {
      |          "period": "1",
      |          "periodKey": "25A1",
      |          "startDate": "20211x5-04-01",
      |          "endDate": "2025-06-30",
      |          "dueDate": "2025-06-30",
      |          "amount": 22945.23
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  "calling getPenaltiesDataForVRN" should {

      "return a successful response and StandingRequest model from the getStandingRequests API" in new Test {

        val responseBody = jsonStandingRequestScheduleMinimumValid

        VatSubscriptionStub.getStandingRequestsSuccess("123")(responseBody)
        val expectedContent: StandingRequest = modelStandingRequestScheduleMinimumValid

        val result = await(connector.getStandingRequests("123"))
        result shouldBe Right(expectedContent)
      }

      "return an Error for invalid response from  the getStandingRequests API" in new Test {

        VatSubscriptionStub.getStandingRequestsError("123")

        val result: HttpResult[StandingRequest] = await(connector.getStandingRequests("123"))
        private val HttpErrorCode = 500
        result shouldBe Left(ServerSideError(HttpErrorCode,"{\"code\":\"Error\"}"))
      }

  }
}