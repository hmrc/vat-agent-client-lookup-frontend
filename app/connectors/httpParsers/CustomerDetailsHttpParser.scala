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

package connectors.httpParsers

import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.CustomerDetails
import models.errors._
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggingUtil

object CustomerDetailsHttpParser extends LoggingUtil {

  implicit object CustomerDetailsReads extends HttpReads[HttpResult[CustomerDetails]] {

    override def read(method: String, url: String, response: HttpResponse): HttpResult[CustomerDetails] = {
      implicit val res: HttpResponse = response

      response.status match {
        case OK =>
          debug("[CustomerCircumstancesHttpParser][read]: Status OK")
          response.json.validate[CustomerDetails].fold(
            invalid => {
              warnLogRes(s"[CustomerCircumstancesHttpParser][read]: Invalid Json - $invalid")
              Left(UnexpectedError(INTERNAL_SERVER_ERROR, "Invalid Json"))
            },
            valid => Right(valid)
          )

        case PRECONDITION_FAILED =>
          errorLogRes(s"[CustomerCircumstancesHttpParser][read]: Status $PRECONDITION_FAILED")
          Left(Migration)

        case NOT_FOUND =>
          errorLogRes(s"[CustomerCircumstancesHttpParser][read]: Status $NOT_FOUND")
          Left(NotSignedUp)

        case status =>
          errorLogRes(
            s"[CustomerCircumstancesHttpParser][read]: - Unexpected Response " +
              s"Status $status returned, with response: ${response.body}"
          )
          Left(UnexpectedError(status, response.body))
      }
    }
  }
}
