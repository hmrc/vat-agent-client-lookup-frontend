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

package stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import helpers.WireMockMethods
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsObject, JsValue, Json}

object FinancialDataStub extends WireMockMethods {

  private def directDebitUri(vrn: String) = s"/financial-transactions/has-direct-debit/$vrn"

  private def paymentUri(vrn: String) = s"/financial-transactions/vat/$vrn"

  val directDebitJson: JsObject = Json.obj("directDebitMandateFound" -> false)
  val errorJson: JsObject = Json.obj("FAILURE" -> "Oh dear")

  private val paidTransactions: JsValue = Json.parse(
    s"""{
       |    "idType" : "VRN",
       |    "idNumber" : "555555555",
       |    "regimeType" : "VATC",
       |    "processingDate" : "2018-03-07T09:30:00.000Z",
       |    "financialTransactions" : [
       |      {
       |        "chargeType" : "ReturnDebitCharge",
       |        "mainType" : "VAT Return Charge",
       |        "periodKey" : "17AA",
       |        "periodKeyDescription" : "ABCD",
       |        "taxPeriodFrom" : "2018-08-01",
       |        "taxPeriodTo" : "2018-10-31",
       |        "businessPartner" : "0",
       |        "contractAccountCategory" : "99",
       |        "contractAccount" : "X",
       |        "contractObjectType" : "ABCD",
       |        "contractObject" : "0",
       |        "sapDocumentNumber" : "0",
       |        "sapDocumentNumberItem" : "0",
       |        "chargeReference" : "XD002750002155",
       |        "mainTransaction" : "1234",
       |        "subTransaction" : "5678",
       |        "originalAmount" : 150,
       |        "items" : [
       |          {
       |            "subItem" : "000",
       |            "clearingDate" : "2018-01-10",
       |            "dueDate" : "2018-12-07",
       |            "paymentAmount" : 150
       |          }
       |        ]
       |      },
       |      {
       |        "chargeType" : "ReturnCreditCharge",
       |        "mainType" : "VAT Return Charge",
       |        "periodKey" : "17BB",
       |        "periodKeyDescription" : "ABCD",
       |        "taxPeriodFrom" : "2018-05-01",
       |        "taxPeriodTo" : "2018-07-31",
       |        "businessPartner" : "0",
       |        "contractAccountCategory" : "99",
       |        "contractAccount" : "X",
       |        "contractObjectType" : "ABCD",
       |        "contractObject" : "0",
       |        "sapDocumentNumber" : "0",
       |        "sapDocumentNumberItem" : "0",
       |        "chargeReference" : "XD002750002155",
       |        "mainTransaction" : "1234",
       |        "subTransaction" : "5678",
       |        "originalAmount" : -600,
       |        "items" : [
       |          {
       |            "subItem" : "000",
       |            "clearingDate" : "2018-03-10",
       |            "dueDate" : "2018-09-07",
       |            "paymentAmount" : -600
       |          }
       |        ]
       |      }
       |    ]
       |  }""".stripMargin
  )

  def getDirectDebitSuccess(vrn: String): StubMapping =
    when(method = GET, uri = directDebitUri(vrn))
      .thenReturn(status = OK, body = directDebitJson)

  def getDirectDebitFailure(vrn: String): StubMapping =
    when(method = GET, uri = directDebitUri(vrn))
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = errorJson)

  def getPaymentSuccess(vrn: String): StubMapping =
    when(method = GET, uri = paymentUri(vrn))
      .thenReturn(status = OK, body = paidTransactions)

  def getPaymentFailure(vrn: String): StubMapping =
    when(method = GET, uri = paymentUri(vrn))
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = errorJson)

}
