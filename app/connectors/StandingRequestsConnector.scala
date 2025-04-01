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

package connectors

import config.AppConfig
import connectors.httpParsers.ResponseHttpParser.HttpResult
import connectors.httpParsers.StandingRequestsHttpParser.StandingRequestsResponseReads
import models.StandingRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StandingRequestsConnector @Inject()(http: HttpClient, appConfig: AppConfig) extends LoggingUtil {

  private[connectors] def standingRequestsUrl(vrn: String): String = s"${appConfig.vatSubscriptionUrl}/vat-subscription/$vrn/standing-requests"

  def getStandingRequests(vrn: String)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[StandingRequest]] = {
    val url = standingRequestsUrl(vrn)
    debug(s"[StandingRequestsConnector][getStandingRequests]: Calling getStandingRequests with URL - $url")
    http.GET(url)(StandingRequestsResponseReads, hc, ec)
  }
}
