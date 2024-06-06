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

import java.time.LocalDate
import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.{Charge, DirectDebit,VatDetailsDataModel}

object FinancialDataConstants {

  val ddNoMandateFound: HttpResult[DirectDebit] = Right(DirectDebit(
    directDebitMandateFound = false
  ))

  val ddMandateFound: HttpResult[DirectDebit] = Right(DirectDebit(
    directDebitMandateFound = true
  ))

  val ddFailureResponse = Left(models.errors.UnexpectedError(500, "problems"))

  val validChargeType = "VAT Repayment Supplement Rec"
  val outstanding = 200.00

  val paymentResponse: HttpResult[Seq[Charge]] =
    Right(Seq(Charge(validChargeType, outstanding, LocalDate.parse("2018-01-01"), ddCollectionInProgress = false)))


  val paymentOverdue = Charge(validChargeType, outstanding, LocalDate.parse("2018-01-01"), ddCollectionInProgress = false)

  val paymentsOverdue =
    Seq(
      Charge(validChargeType, outstanding, LocalDate.parse("2018-01-01"), ddCollectionInProgress = false),
      Charge(validChargeType, outstanding, LocalDate.parse("2020-01-01"), ddCollectionInProgress = false)
    )
  val paymentsNotOverdue =
    Seq(
      Charge(validChargeType, outstanding, LocalDate.parse("2020-01-01"), ddCollectionInProgress = false),
      Charge(validChargeType, outstanding, LocalDate.parse("2020-01-01"), ddCollectionInProgress = false)
    )

  val paymentNoOutstandingAmount = Seq(Charge(
    chargeType = validChargeType,
    outstandingAmount = 0,
    dueDate = LocalDate.parse("2020-01-01"),
    ddCollectionInProgress = false
  ))

  val paymentsInclOnAccount: Seq[Charge] =
    paymentsNotOverdue ++ Seq(Charge(
        chargeType = "Payment on account",
        outstandingAmount = 200.00,
        dueDate = LocalDate.parse("2020-01-01"),
        ddCollectionInProgress = false
    ))
  val paymentOnAccountResponse: HttpResult[Seq[Charge]] = Right(paymentsInclOnAccount)

  val onePaymentModelOverdue: VatDetailsDataModel = VatDetailsDataModel(
    payments = Seq(paymentOverdue), isError = false
  )

  val paymentsModelOneOverdue: VatDetailsDataModel = VatDetailsDataModel(paymentsOverdue, isError = false)
  val paymentsModelNoneOverdue: VatDetailsDataModel = VatDetailsDataModel(paymentsNotOverdue, isError = false)
  val paymentsModelNoPayments: VatDetailsDataModel = VatDetailsDataModel(Seq(), isError = false)
  val paymentsModel1OnAccount: VatDetailsDataModel = VatDetailsDataModel(paymentsInclOnAccount, isError = false)

}
