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

package models

import java.time.LocalDate
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json

class ChargeSpec extends AnyWordSpecLike with Matchers {

  val startDate = "2017-01-01"
  val endDate = "2017-03-01"
  val dueDate = "2017-03-08"
  val outstandingAmount = 9999

  "Financial Transactions JSON should parse to Charge model correctly" when {

    "there is only one payment due" in {

      val paymentJson = Json.arr(
        Json.obj(
          "chargeType" -> "VAT Return Debit Charge",
          "taxPeriodFrom" -> startDate,
          "taxPeriodTo" -> endDate,
          "items" -> Json.arr(
            Json.obj(
              "dueDate" -> dueDate
            )
          ),
          "outstandingAmount" -> outstandingAmount,
          "periodKey" -> "#001",
          "chargeReference" -> "XD002750002155"
        )
      )
      val paymentWithOnePaymentDue = Seq(Charge("VAT Return Debit Charge", outstandingAmount, LocalDate.parse(dueDate), ddCollectionInProgress = false))
      paymentJson.as[Seq[Charge]] shouldBe paymentWithOnePaymentDue
    }

    "there are multiple payments due" in {

      val paymentsJson = Json.arr(
        Json.obj(
          "chargeType" -> "VAT Return Debit Charge",
          "taxPeriodFrom" -> startDate,
          "taxPeriodTo" -> endDate,
          "items" -> Json.arr(
            Json.obj(
              "dueDate" -> dueDate
            )
          ),
          "outstandingAmount" -> outstandingAmount,
          "periodKey" -> "#001",
          "chargeReference" -> "XD002750002155"
        ),
        Json.obj(
          "chargeType" -> "VAT Return Debit Charge",
          "taxPeriodFrom" -> startDate,
          "taxPeriodTo" -> endDate,
          "items" -> Json.arr(
            Json.obj(
              "dueDate" -> "2017-03-15"
            )
          ),
          "outstandingAmount" -> outstandingAmount,
          "periodKey" -> "#001",
          "chargeReference" -> "XD002750002155"
        ),
        Json.obj(
          "chargeType" -> "VAT Return Debit Charge",
          "taxPeriodFrom" -> startDate,
          "taxPeriodTo" -> endDate,
          "items" -> Json.arr(
            Json.obj(
              "dueDate" -> "2017-03-20"
            )
          ),
          "outstandingAmount" -> outstandingAmount,
          "periodKey" -> "#001",
          "chargeReference" -> "XD002750002155"
        )
      )

      val paymentsWithFewPaymentDue = Seq(
        Charge("VAT Return Debit Charge", outstandingAmount, LocalDate.parse(dueDate), ddCollectionInProgress = false),
        Charge("VAT Return Debit Charge", outstandingAmount, LocalDate.parse("2017-03-15"), ddCollectionInProgress = false),
        Charge("VAT Return Debit Charge", outstandingAmount, LocalDate.parse("2017-03-20"), ddCollectionInProgress = false)
      )

      paymentsJson.as[Seq[Charge]] shouldBe paymentsWithFewPaymentDue
    }

    "there is a partial payment" in {

      val partialPaymentJson = Json.arr(
        Json.obj(
          "chargeType" -> "VAT Return Debit Charge",
          "taxPeriodFrom" -> startDate,
          "taxPeriodTo" -> endDate,
          "originalAmount" -> 100,
          "items" -> Json.arr(
            Json.obj(
              "dueDate" -> dueDate,
              "amount" -> 80,
              "DDcollectionInProgress" -> true
            ),
            Json.obj(
              "dueDate" -> dueDate,
              "paymentAmount" -> 20
            )
          ),
          "outstandingAmount" -> outstandingAmount,
          "periodKey" -> "#001",
          "chargeReference" -> "XD002750002155"
        )
      )
      val partialPayment = Seq(
        Charge("VAT Return Debit Charge", outstandingAmount, LocalDate.parse(dueDate), ddCollectionInProgress = true)
      )
      partialPaymentJson.as[Seq[Charge]] shouldBe partialPayment
    }
  }
}
