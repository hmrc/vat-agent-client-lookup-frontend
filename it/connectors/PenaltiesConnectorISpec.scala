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

import helpers.IntegrationBaseSpec
import models.penalties.PenaltiesSummary
import play.api.libs.json.Json
import play.api.test.Helpers._
import stubs.PenaltiesStub
import uk.gov.hmrc.http.HeaderCarrier

class PenaltiesConnectorISpec extends IntegrationBaseSpec {

  private trait Test {
    val connector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

  "calling getPenaltiesDataForVRN" should {

      "return a successful response and PenaltySummary model from the penalties API" in new Test {

        val responseBody = Json.parse(
          """
            |{
            |  "noOfPoints": 3,
            |  "noOfEstimatedPenalties": 2,
            |  "noOfCrystalisedPenalties": 1,
            |  "estimatedPenaltyAmount": 123.45,
            |  "crystalisedPenaltyAmountDue": 54.32,
            |  "hasAnyPenaltyData": true
            |}
            |""".stripMargin)
        PenaltiesStub.stubPenaltiesSummary(OK, responseBody, "123")
        val expectedContent: PenaltiesSummary = PenaltiesSummary(
          noOfPoints = 3,
          noOfEstimatedPenalties = 2,
          noOfCrystalisedPenalties = 1,
          estimatedPenaltyAmount = 123.45,
          crystalisedPenaltyAmountDue = 54.32,
          hasAnyPenaltyData = true
        )

        val result = await(connector.getPenaltiesDataForVRN("123"))
        result shouldBe Right(expectedContent)
      }

      "return an Empty PenaltiesSummary model when given an invalid vrn" in new Test {
        val responseBody = Json.parse(
          """
            |{
            | "code": "foo",
            | "message": "bar"
            |}
            |""".stripMargin)
        PenaltiesStub.stubPenaltiesSummary(NOT_FOUND, responseBody, "123")
        val expectedContent: PenaltiesSummary = PenaltiesSummary.empty

        val result = await(connector.getPenaltiesDataForVRN("123"))
        result shouldBe Right(expectedContent)
      }

  }
}