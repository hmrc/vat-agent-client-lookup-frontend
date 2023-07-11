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
import models.errors.UnexpectedError
import play.api.http.Status.{CONFLICT, CREATED}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggingUtil

object RequestPasscodeHttpParser extends LoggingUtil {

  implicit object RequestPasscodeHttpReads extends HttpReads[HttpResult[EmailVerificationPasscodeRequest]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[EmailVerificationPasscodeRequest] = {
      implicit val res: HttpResponse = response
      response.status match {
        case CREATED =>
          debug("[RequestPasscodeHttpParser][RequestPasscodeHttpReads][read] - Email passcode request sent successfully")
          Right(EmailVerificationPasscodeRequestSent)
        case CONFLICT =>
          debug("[RequestPasscodeHttpParser][RequestPasscodeHttpReads][read] - Email already verified")
          Right(EmailIsAlreadyVerified)
        case status =>
          errorLogRes(
            "[RequestPasscodeHttpParser][RequestPasscodeHttpReads][read] - " +
              s"Failed to create email verification passcode. Received status: $status, Response body: ${response.body}"
          )
          Left(UnexpectedError(status, response.body))
      }
    }
  }

  sealed trait EmailVerificationPasscodeRequest

  object EmailIsAlreadyVerified extends EmailVerificationPasscodeRequest

  object EmailVerificationPasscodeRequestSent extends EmailVerificationPasscodeRequest

}
