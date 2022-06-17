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

package services

import connectors.FinancialDataConnector
import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.{Charge, DirectDebit}
import uk.gov.hmrc.http.HeaderCarrier
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinancialDataService @Inject()(financialDataConnector: FinancialDataConnector) {

  def getDirectDebit(vrn: String)(implicit hc: HeaderCarrier): Future[HttpResult[DirectDebit]] =
    financialDataConnector.getDirectDebit(vrn)

  def getPayment(vrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[Seq[Charge]]] = {
    financialDataConnector.getPaymentsDue(vrn) map {
      case Right(payments) => Right(payments.filter(_.outstandingAmount > 0))
      case Left(error) => Left(error)
    }
  }
}
