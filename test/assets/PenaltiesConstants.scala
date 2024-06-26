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

import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.errors.UnexpectedError
import models.penalties.PenaltiesSummary
import play.api.libs.json.{JsValue, Json}

object PenaltiesConstants {
  val penaltiesSummaryJson: JsValue = Json.parse(
    """
      |{
      |  "noOfPoints": 3,
      |  "noOfEstimatedPenalties": 2,
      |  "noOfCrystalisedPenalties": 1,
      |  "estimatedPenaltyAmount": 123.45,
      |  "crystalisedPenaltyAmountDue": 54.32,
      |  "hasAnyPenaltyData": true
      |}
      |""".stripMargin
  )

  val penaltiesSummaryAsModel: PenaltiesSummary = PenaltiesSummary(
    noOfPoints = 3,
    noOfEstimatedPenalties = 2,
    noOfCrystalisedPenalties = 1,
    estimatedPenaltyAmount = BigDecimal(123.45),
    crystalisedPenaltyAmountDue = BigDecimal(54.32),
    hasAnyPenaltyData = true
  )

  val penaltiesSummaryAsModelNoPenalties: PenaltiesSummary = PenaltiesSummary(
    noOfPoints = 0,
    noOfEstimatedPenalties = 0,
    noOfCrystalisedPenalties = 0,
    estimatedPenaltyAmount = BigDecimal(0),
    crystalisedPenaltyAmountDue = BigDecimal(0),
    hasAnyPenaltyData = false
  )

  val penaltiesSummaryResponse: HttpResult[PenaltiesSummary] = Right(penaltiesSummaryAsModel)
  val penaltiesSummaryNoPenaltiesResponse: HttpResult[PenaltiesSummary] = Right(penaltiesSummaryAsModelNoPenalties)
  val penaltiesSummaryNotFoundResponse: HttpResult[PenaltiesSummary] = Left(UnexpectedError(404, "Not found"))
}
