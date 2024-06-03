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

package services

import connectors.FinancialDataConnector
import models.DirectDebit
import models.errors.UnexpectedError
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import assets.FinancialDataConstants.{paymentNoOutstandingAmount, paymentsNotOverdue}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Request
import play.api.test.FakeRequest

import scala.concurrent.{ExecutionContext, Future}

class FinancialDataServiceSpec extends AnyWordSpecLike with Matchers with MockitoSugar with GuiceOneAppPerSuite {

  val mockConnector: FinancialDataConnector = mock[FinancialDataConnector]
  val service = new FinancialDataService(mockConnector)
  val vrn = "999999999"
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val request: Request[_] = FakeRequest()

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

  "PaymentService" when {

    "there is a successful response with outstanding amounts" should {

      "return the result of the connector call" in {
        val response = Right(paymentsNotOverdue)

        when(mockConnector.getPaymentsDue(vrn)(hc)).thenReturn(Future.successful(response))
        await(service.getPayment(vrn)) shouldBe response
      }
    }

    "one of the charges has an outstanding amount of 0" should {

      "return all charges except that one" in {
        val response = Right(paymentsNotOverdue ++ paymentNoOutstandingAmount)
        val result = Right(paymentsNotOverdue)

        when(mockConnector.getPaymentsDue(vrn)(hc)).thenReturn(Future.successful(response))
        await(service.getPayment(vrn)) shouldBe result
      }
    }

    "there is an error response" in {
      when(mockConnector.getPaymentsDue(vrn)(hc))
        .thenReturn(Future.successful(Left(UnexpectedError(INTERNAL_SERVER_ERROR, "Fail"))))
      await(service.getPayment(vrn)) shouldBe Left(UnexpectedError(INTERNAL_SERVER_ERROR, "Fail"))
    }
  }
}
