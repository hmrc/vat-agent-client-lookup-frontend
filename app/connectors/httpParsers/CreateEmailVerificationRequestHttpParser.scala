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
import play.api.http.Status.{CONFLICT, CREATED}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggingUtil

object CreateEmailVerificationRequestHttpParser extends LoggingUtil {

  implicit object CreateEmailVerificationRequestHttpReads extends HttpReads[HttpResult[EmailVerificationRequest]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[EmailVerificationRequest] = {
      implicit val res: HttpResponse = response
      response.status match {
        case CREATED =>
          debug("[CreateEmailVerificationRequestHttpReads][read] - Email request sent successfully")
          Right(EmailVerificationRequestSent)
        case CONFLICT =>
          debug("[CreateEmailVerificationRequestHttpReads][read] - Email already verified")
          Right(EmailAlreadyVerified)
        case status =>
          errorLogRes(
            "[CreateEmailVerificationRequestHttpParser][CreateEmailVerificationRequestHttpReads][read] - " +
              s"Failed to create email verification. Received status: $status Response body: ${response.body}"
          )
          Left(UnexpectedError(status, response.body))
      }
    }
  }

  sealed trait EmailVerificationRequest

  object EmailAlreadyVerified extends EmailVerificationRequest

  object EmailVerificationRequestSent extends EmailVerificationRequest
}
