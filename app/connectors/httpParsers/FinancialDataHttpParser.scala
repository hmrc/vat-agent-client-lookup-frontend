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

package connectors.httpParsers

import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.errors.UnexpectedError
import models.{Charge, DirectDebit}
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggingUtil

object FinancialDataHttpParser extends LoggingUtil {

  implicit object DirectDebitReads extends HttpReads[HttpResult[DirectDebit]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[DirectDebit] = {
      implicit val res: HttpResponse = response
      response.status match {
        case OK => Right(response.json.as[DirectDebit])
        case status =>
          errorLogRes(
            "[FinancialDataHttpParser][DirectDebitReads][read] - " +
              s"Unexpected Response, Status $status returned, with response: ${response.body}"
          )
          Left(UnexpectedError(status, response.body))
      }
    }
  }

  implicit object ChargeReads extends HttpReads[HttpResult[Seq[Charge]]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[Seq[Charge]] = {
      implicit val res: HttpResponse = response
      response.status match {
        case OK => Right((response.json \ "financialTransactions").as[Seq[Charge]])
        case NOT_FOUND => Right(Seq())
        case status =>
          errorLogRes(
            "[FinancialDataHttpParser][ChargeReads][read] - " +
              s"Unexpected Response, Status $status returned, with response: ${response.body}"
          )
          Left(UnexpectedError(status, response.body))
      }
    }
  }

}
