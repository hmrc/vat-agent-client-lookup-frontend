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

import connectors.FinancialDataConnector
import models.{Charge, DirectDebit}
import models.errors.UnexpectedError
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import java.time.LocalDate
import scala.concurrent.Future

class FinancialDataServiceSpec extends AnyWordSpecLike with Matchers with MockitoSugar {

  val mockConnector: FinancialDataConnector = mock[FinancialDataConnector]
  val service = new FinancialDataService(mockConnector)
  val vrn = "999999999"
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "DirectDebitService should return the result of the connector call" when {

    "there is a successful response" in {
      when(mockConnector.getDirectDebit(vrn)(hc)).thenReturn(Future.successful(Right(DirectDebit(false))))
      await(service.getDirectDebit(vrn)) shouldBe Right(DirectDebit(false))
    }

    "there is an error response" in {
      when(mockConnector.getDirectDebit(vrn)(hc))
        .thenReturn(Future.successful(Left(UnexpectedError(INTERNAL_SERVER_ERROR, "Fail"))))
      await(service.getDirectDebit(vrn)) shouldBe Left(UnexpectedError(INTERNAL_SERVER_ERROR, "Fail"))
    }
  }

  "PaymentService should return the result of the connector call" when {

    "there is a successful response" in {
      val response = Right(Seq(Charge(LocalDate.parse("2019-09-13"), ddCollectionInProgress = false)))

      when(mockConnector.getPaymentsDue(vrn)(hc)).thenReturn(Future.successful(response))
      await(service.getPayment(vrn)) shouldBe response
    }

    "there is an error response" in {
      when(mockConnector.getPaymentsDue(vrn)(hc))
        .thenReturn(Future.successful(Left(UnexpectedError(INTERNAL_SERVER_ERROR, "Fail"))))
      await(service.getPayment(vrn)) shouldBe Left(UnexpectedError(INTERNAL_SERVER_ERROR, "Fail"))
    }
  }
}
