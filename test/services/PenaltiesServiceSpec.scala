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

package services

import assets.PenaltiesConstants.penaltiesSummaryAsModel
import connectors.PenaltiesConnector
import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.penalties.PenaltiesSummary
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PenaltiesServiceSpec extends AnyWordSpecLike with Matchers with MockitoSugar {

  val penaltiesSummary: HttpResult[PenaltiesSummary] = Right(penaltiesSummaryAsModel)
  val vrn = "999999999"
  val mockPenaltiesConnector: PenaltiesConnector = mock[PenaltiesConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val penaltiesService: PenaltiesService = new PenaltiesService(mockPenaltiesConnector)

  "Calling getPenaltiesDataForVRN" should {
    "retrieve the penalties summary for the vrn" in {
      when(mockPenaltiesConnector.getPenaltiesDataForVRN(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right(penaltiesSummaryAsModel)))
      val summary: HttpResult[PenaltiesSummary] = await(penaltiesService.getPenaltiesInformation("123"))
      summary shouldBe penaltiesSummary
    }
  }
}
