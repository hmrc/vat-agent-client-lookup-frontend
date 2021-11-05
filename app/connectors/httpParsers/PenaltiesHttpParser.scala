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
import models.errors.UnexpectedError
import models.penalties.PenaltiesSummary
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggerUtil

object PenaltiesHttpParser extends LoggerUtil {

  implicit object PenaltiesReads extends HttpReads[HttpResult[PenaltiesSummary]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[PenaltiesSummary] = {
      response.status match {
        case OK => Right(response.json.as[PenaltiesSummary])
        case NOT_FOUND => Right(PenaltiesSummary.empty)
        case status =>
          logger.warn(s"[PenaltiesHttpParser][PenaltiesReads][read] - Received unexpected error. Response status: $status, Response body: ${response.body}")
          Left(UnexpectedError(status, response.body))
      }
    }
  }

}