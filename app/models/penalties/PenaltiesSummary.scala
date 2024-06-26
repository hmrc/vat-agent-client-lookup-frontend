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

package models.penalties

import play.api.libs.json.{Json, Reads}

case class PenaltiesSummary(noOfPoints: Int,
                            noOfEstimatedPenalties: Int,
                            noOfCrystalisedPenalties: Int,
                            estimatedPenaltyAmount: BigDecimal,
                            crystalisedPenaltyAmountDue: BigDecimal,
                            hasAnyPenaltyData: Boolean) {

  val hasActivePenalties: Boolean = noOfPoints > 0 || noOfCrystalisedPenalties > 0  || noOfEstimatedPenalties > 0

  val hasMultiplePenalties: Boolean = {
     noOfPoints + noOfEstimatedPenalties + noOfCrystalisedPenalties > 1
  }
}

object PenaltiesSummary {
  implicit val reads: Reads[PenaltiesSummary] = Json.reads[PenaltiesSummary]

  def empty: PenaltiesSummary = PenaltiesSummary(
    noOfPoints = 0,
    noOfEstimatedPenalties = 0,
    noOfCrystalisedPenalties = 0,
    estimatedPenaltyAmount = 0,
    crystalisedPenaltyAmountDue = 0,
    hasAnyPenaltyData = false
  )
}

