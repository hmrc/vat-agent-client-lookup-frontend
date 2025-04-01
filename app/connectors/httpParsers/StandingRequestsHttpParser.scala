/*
 * Copyright 2025 HM Revenue & Customs
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
import models.StandingRequest
import models.errors.{BadRequestError, ServerSideError, UnexpectedError}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggingUtil

object StandingRequestsHttpParser extends LoggingUtil {

  implicit object StandingRequestsResponseReads extends HttpReads[HttpResult[StandingRequest]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[StandingRequest] = {

      response.status match {
        case OK =>
          Json.parse(response.body).validate[StandingRequest] match {
            case JsSuccess(standingRequestResponse, _) =>
              Right(standingRequestResponse)
            case JsError(errors) =>
              Left(UnexpectedError(INTERNAL_SERVER_ERROR, s"JSON Parsing Error: $errors"))
          }

        case NOT_FOUND => Right(StandingRequest("", List.empty))
        case BAD_REQUEST => Left(BadRequestError(response.status, response.body))
        case status if status >= 500 && status < 600 => Left(ServerSideError(response.status, response.body))
        case _ => Left(UnexpectedError(response.status, response.body))
      }
    }
  }
}