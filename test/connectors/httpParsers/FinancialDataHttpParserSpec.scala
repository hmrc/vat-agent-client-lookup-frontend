/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors.httpParsers

import connectors.httpParsers.FinancialDataHttpParser.ChargeReads
import models.Charge
import models.errors.UnexpectedError
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtil
import java.time.LocalDate

class FinancialDataHttpParserSpec extends TestUtil {

  val chargeType: String = "VAT Return Debit Charge"
  val poaCharge: String = "Payment on account"
  val outstandingAmountJson: Double = 1500.00
  val outstandingAmount: BigDecimal = 1500

  "ChargeReads" when {

    "the http response status is OK with valid Json" should {

      val responseJson = Json.obj(
        "financialTransactions" -> Json.arr(
          Json.obj(
            "chargeType" -> chargeType,
            "items" -> Json.arr(
              Json.obj(
                "dueDate" -> "2020-01-01",
                "DDcollectionInProgress" -> true
              )
            ),
            "outstandingAmount" -> outstandingAmountJson,
            "chargeReference" -> "XD002750002155"
          ),
          Json.obj(
            "chargeType" -> chargeType,
            "items" -> Json.arr(
              Json.obj(
                "dueDate" -> "2020-02-02"
              )
            ),
            "outstandingAmount" -> outstandingAmountJson,
            "chargeReference" -> "XD002750002155"
          ),
          Json.obj(
            "chargeType" -> chargeType,
            "items" -> Json.arr(
              Json.obj(
                "dueDate" -> "2020-03-03"
              )
            ),
            "outstandingAmount" -> outstandingAmountJson,
            "chargeReference" -> "XD002750002155"
          )
        )
      )

      val expectedModel = Seq(
        Charge(chargeType, outstandingAmount, LocalDate.parse("2020-01-01"), ddCollectionInProgress = true),
        Charge(chargeType, outstandingAmount, LocalDate.parse("2020-02-02"), ddCollectionInProgress = false),
        Charge(chargeType, outstandingAmount, LocalDate.parse("2020-03-03"), ddCollectionInProgress = false)
      )

      "return a collection of charges" in {
        ChargeReads.read("", "", HttpResponse(Status.OK, responseJson.toString())) shouldBe Right(expectedModel)
      }
    }

    "the http response status is NOT_FOUND (404)" should {
      val httpResponse = HttpResponse(Status.NOT_FOUND, "")

      val result = ChargeReads.read("", "", httpResponse)

      val expectedModel = Seq()

      "return an empty Charge object" in {
        result shouldBe Right(expectedModel)
      }
    }

    "the http response status is BAD_REQUEST (400) with unknown error Json" should {

      val httpResponse = HttpResponse(Status.BAD_REQUEST,
        Json.obj(
          "error" -> "400",
          "type" -> "Unknown Json"
        ).toString()
      )

      val errorModel = Left(UnexpectedError(400, httpResponse.body))

      val result = ChargeReads.read("", "", httpResponse)

      "return an UnknownError" in {
        result shouldBe errorModel
      }
    }

  }
}
