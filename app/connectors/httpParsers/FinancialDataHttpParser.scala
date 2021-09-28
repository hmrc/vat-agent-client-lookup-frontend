/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.{Charge, DirectDebit}
import models.errors.UnexpectedError
import play.api.Logger
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object FinancialDataHttpParser {

  implicit object DirectDebitReads extends HttpReads[HttpResult[DirectDebit]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[DirectDebit] = {
      response.status match {
        case OK => Right(response.json.as[DirectDebit])
        case status =>
          Logger.warn(
            "[FinancialDataHttpParser][DirectDebitReads][read] - " +
              s"Unexpected Response, Status $status returned, with response: ${response.body}"
          )
          Left(UnexpectedError(status, response.body))
      }
    }
  }

  implicit object ChargeReads extends HttpReads[HttpResult[Seq[Charge]]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[Seq[Charge]] = {
      response.status match {
        case OK => Right((response.json \ "financialTransactions").as[Seq[Charge]])
        case NOT_FOUND => Right(Seq())
        case status =>
          Logger.warn(
            "[FinancialDataHttpParser][ChargeReads][read] - " +
              s"Unexpected Response, Status $status returned, with response: ${response.body}"
          )
          Left(UnexpectedError(status, response.body))
      }
    }
  }

}
