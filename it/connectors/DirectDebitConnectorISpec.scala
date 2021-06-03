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
import models.DirectDebit
import models.errors.UnexpectedError
import play.api.http.Status.INTERNAL_SERVER_ERROR
import stubs.DirectDebitStub
import uk.gov.hmrc.http.HeaderCarrier

class DirectDebitConnectorISpec extends IntegrationBaseSpec {

  val connector: DirectDebitConnector = app.injector.instanceOf[DirectDebitConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val vrn = "999999999"

  "Calling getDirectDebitSuccess" when {

    "the endpoint returns a successful Direct Debit response" should {

      "parse the JSON response and return a DirectDebit model" in {
        DirectDebitStub.getDirectDebitSuccess(vrn)
        val expected = Right(DirectDebit(false))
        val result: HttpResult[DirectDebit] = await(connector.getDirectDebit(vrn))

        result shouldBe expected
      }
    }

    "the endpoint returns a failure response" should {

      "return an UnexpectedError model containing the response body" in {
        DirectDebitStub.getDirectDebitFailure(vrn)
        val expected = Left(UnexpectedError(INTERNAL_SERVER_ERROR, """{"FAILURE":"Oh dear"}"""))
        val result: HttpResult[DirectDebit] = await(connector.getDirectDebit(vrn))

        result shouldBe expected
      }
    }
  }
}
