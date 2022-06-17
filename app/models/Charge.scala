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

package models

import play.api.libs.json.{JsPath, Reads}
import java.time.LocalDate

import play.api.libs.functional.syntax.{toAlternativeOps, toFunctionalBuilderOps}

case class Charge(chargeType: String,
                  outstandingAmount: BigDecimal,
                  dueDate: LocalDate,
                  ddCollectionInProgress: Boolean)

object Charge {

  val validChargeTypes: Seq[String] = Seq(
    "VAT Unrepayable Overpayment",
    "VAT Repayment Supplement Rec",
    "VAT Indirect Tax Revenue Rec",
    "VAT Default Interest",
    "VAT Further Interest",
    "VAT Return Debit Charge",
    "VAT Return Credit Charge",
    "VAT OA Credit Charge",
    "VAT OA Debit Charge",
    "VAT OA Default Interest",
    "VAT OA Further Interest",
    "VAT Debit Default Surcharge",
    "VAT Credit Default Surcharge",
    "VAT Central Assessment",
    "VAT EC Credit Charge",
    "VAT EC Debit Charge",
    "VAT Repayment Supplement",
    "VAT AA Default Interest",
    "VAT AA Further Interest",
    "VAT Additional Assessment",
    "VAT AA Quarterly Instalments",
    "VAT AA Monthly Instalment",
    "VAT AA Return Debit Charge",
    "VAT AA Return Credit Charge",
    "VAT BNP of Reg Pre 2010",
    "VAT BNP of Reg Post 2010",
    "VAT FTN Mat Change Pre 2010",
    "VAT FTN Mat Change Post 2010",
    "VAT FTN Each Partner",
    "VAT Miscellaneous Penalty",
    "VAT MP pre 2009",
    "VAT MP (R) pre 2009",
    "VAT Civil Evasion Penalty",
    "VAT OA Inaccuracies from 2009",
    "VAT Inaccuracy Assessments pen",
    "VAT Inaccuracy return replaced",
    "VAT Wrong Doing Penalty",
    "VAT Carter Penalty",
    "VAT FTN RCSL",
    "VAT Failure to submit RCSL",
    "VAT Inaccuracies in EC Sales",
    "VAT EC Default Interest",
    "VAT EC Further Interest",
    "VAT Security Deposit Request",
    "VAT Protective Assessment",
    "VAT PA Default Interest",
    "VAT Failure to Submit EC Sales",
    "VAT Statutory Interest",
    "VAT PA Further Interest",
    "Credit Return Offset",
    "VAT POA Return Debit Charge",
    "VAT POA Return Credit Charge",
    "VAT POA Instalments",
    "Unallocated payment",
    "Refund",
    "VAT Return 1st LPP"
  )

  implicit val reads: Reads[Charge] = (
    (JsPath \ "chargeType").read[String] and
    (JsPath \ "outstandingAmount").read[BigDecimal] and
    (JsPath \ "items")(0).\("dueDate").read[LocalDate] and
    (JsPath \ "items")(0).\("DDcollectionInProgress").read[Boolean].or(Reads.pure(false))
  ) (Charge.apply _)
}
