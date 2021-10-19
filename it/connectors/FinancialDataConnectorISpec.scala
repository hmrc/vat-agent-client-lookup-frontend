/*
 * Copyright 2019 HM Revenue & Customs
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
import models.{Charge, DirectDebit}
import models.errors.UnexpectedError
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.FinancialDataStub
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class FinancialDataConnectorISpec extends IntegrationBaseSpec {

  val financialDataConnector: FinancialDataConnector = app.injector.instanceOf[FinancialDataConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val vrn = "999999999"

  "Calling getDirectDebit" when {

    "the endpoint returns a successful Direct Debit response" should {

      "parse the JSON response and return a DirectDebit model" in {
        FinancialDataStub.getDirectDebitSuccess(vrn)
        val expected = Right(DirectDebit(false))
        val result: HttpResult[DirectDebit] = await(financialDataConnector.getDirectDebit(vrn))

        result shouldBe expected
      }
    }

    "the endpoint returns a failure response" should {

      "return an UnexpectedError model containing the response body" in {
        FinancialDataStub.getDirectDebitFailure(vrn)
        val expected = Left(UnexpectedError(INTERNAL_SERVER_ERROR, """{"FAILURE":"Oh dear"}"""))
        val result: HttpResult[DirectDebit] = await(financialDataConnector.getDirectDebit(vrn))

        result shouldBe expected
      }
    }
  }

  "Calling getPaymentsDue" when {

    "the endpoint returns a successful payment response" should {

      "parse the JSON response and return a Payment model" in {
        FinancialDataStub.getPaymentSuccess(vrn)
        val expected = Right(Seq(
          Charge(LocalDate.parse("2018-09-13"), ddCollectionInProgress = false),
          Charge(LocalDate.parse("2018-12-11"), ddCollectionInProgress = false)
        ))
        val result: HttpResult[Seq[Charge]] = await(financialDataConnector.getPaymentsDue(vrn))

        result shouldBe expected
      }
    }

    "the endpoint returns a failure response" should {

      "return an UnexpectedError model containing the response body" in {
        FinancialDataStub.getPaymentFailure(vrn)
        val expected = Left(UnexpectedError(INTERNAL_SERVER_ERROR, """{"FAILURE":"Oh dear"}"""))
        val result: HttpResult[Seq[Charge]] = await(financialDataConnector.getPaymentsDue(vrn))

        result shouldBe expected
      }
    }
  }
}
