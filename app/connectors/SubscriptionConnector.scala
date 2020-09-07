/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.httpParsers.CustomerDetailsHttpParser.CustomerDetailsReads
import connectors.httpParsers.ResponseHttpParser.HttpResult
import javax.inject.{Inject, Singleton}
import models.CustomerDetails
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionConnector @Inject()(val http: HttpClient,
                                      val config: AppConfig) {

  private[connectors] def getCustomerDetailsUrl(vrn: String) = s"${config.vatSubscriptionUrl}/vat-subscription/$vrn/full-information"

  def getCustomerDetails(id: String)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[CustomerDetails]] = {
    val url = getCustomerDetailsUrl(id)
    Logger.debug(s"[CustomerDetailsConnector][getCustomerDetails]: Calling getCustomerDetails with URL - $url")
    http.GET(url)(CustomerDetailsReads, headerCarrier, ec)
  }
}
