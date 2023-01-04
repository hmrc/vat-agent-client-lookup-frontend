/*
 * Copyright 2023 HM Revenue & Customs
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

  private def paymentUri(vrn: String) = s"/financial-transactions/vat/$vrn\\?onlyOpenItems=true"

  val directDebitJson: JsObject = Json.obj("directDebitMandateFound" -> false)
  val errorJson: JsObject = Json.obj("FAILURE" -> "Oh dear")

  private val outstandingTransactions: (String, String) => JsValue = (charge1, charge2) => Json.parse(
    s"""{
       |    "idType" : "VRN",
       |    "idNumber" : "555555555",
       |    "regimeType" : "VATC",
       |    "processingDate" : "2018-03-07T09:30:00.000Z",
       |    "financialTransactions" : [
       |      {
       |        "chargeType" : $charge1,
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
       |        "originalAmount" : 10000,
       |        "outstandingAmount" : 10000,
       |        "items" : [
       |          {
       |            "subItem" : "000",
       |            "dueDate" : "2018-09-13",
       |            "amount" : 10000
       |          }
       |        ]
       |      },
       |      {
       |        "chargeType" : $charge2,
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
       |        "originalAmount" : 500,
       |        "outstandingAmount" : 500,
       |        "items" : [
       |          {
       |            "subItem" : "000",
       |            "dueDate" : "2018-12-11",
       |            "amount" : 10000
       |          }
       |        ]
       |      }
       |    ]
       |  }""".stripMargin
  )

  val validChargesJson: JsValue = outstandingTransactions(
    "\"VAT Return Debit Charge\"",
    "\"VAT OA Debit Charge\""
  )

  val invalidChargesJson: JsValue = outstandingTransactions(
    "\"Payment on account\"",
    "\"Invalid Charge\""
  )

  def getDirectDebitSuccess(vrn: String): StubMapping =
    when(method = GET, uri = directDebitUri(vrn))
      .thenReturn(status = OK, body = directDebitJson)

  def getDirectDebitFailure(vrn: String): StubMapping =
    when(method = GET, uri = directDebitUri(vrn))
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = errorJson)

  def getPaymentSuccess(vrn: String, responseBody: JsValue): StubMapping =
    when(method = GET, uri = paymentUri(vrn))
      .thenReturn(status = OK, body = responseBody)

  def getValidPayments: StubMapping = getPaymentSuccess("999999999", validChargesJson)

  def getInvalidPayments: StubMapping = getPaymentSuccess("999999999", invalidChargesJson)

  def getPaymentFailure(vrn: String): StubMapping =
    when(method = GET, uri = paymentUri(vrn))
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = errorJson)

}
