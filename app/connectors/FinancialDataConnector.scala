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

package connectors

import config.AppConfig
import connectors.httpParsers.FinancialDataHttpParser.DirectDebitReads
import connectors.httpParsers.FinancialDataHttpParser.ChargeReads
import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.{Charge, DirectDebit}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinancialDataConnector @Inject()(httpClient: HttpClient,
                                       appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def directDebitUrl(vrn: String): String =
    s"${appConfig.financialTransactionsBaseUrl}/financial-transactions/has-direct-debit/$vrn"

  def getDirectDebit(vrn: String)(implicit hc: HeaderCarrier): Future[HttpResult[DirectDebit]] =
    httpClient.GET(directDebitUrl(vrn))(DirectDebitReads, hc, ec)

  private[connectors] def paymentUrl(vrn: String): String =
    s"${appConfig.financialTransactionsBaseUrl}/financial-transactions/vat/$vrn"

  def getPaymentsDue(vrn: String)(implicit hc: HeaderCarrier): Future[HttpResult[Seq[Charge]]] =
    httpClient.GET(paymentUrl(vrn), Seq("onlyOpenItems" -> "true"))(ChargeReads, hc, ec)
}
