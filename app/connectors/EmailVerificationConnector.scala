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
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import connectors.httpParsers.CreateEmailVerificationRequestHttpParser.EmailVerificationRequest
import connectors.httpParsers.GetEmailVerificationStateHttpParser.EmailVerificationState
import connectors.httpParsers.RequestPasscodeHttpParser.EmailVerificationPasscodeRequest
import connectors.httpParsers.ResponseHttpParser.HttpResult

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationConnector @Inject()(http: HttpClient,
                                           appConfig: AppConfig)
                                          (implicit ec: ExecutionContext) {

  private val checkVerifiedEmailUrl: String =
    s"${appConfig.emailVerificationBaseUrl}/email-verification/verified-email-check"

  private[connectors] lazy val createEmailVerificationRequestUrl: String =
    s"${appConfig.emailVerificationBaseUrl}/email-verification/verification-requests"

  def getEmailVerificationState(emailAddress: String)
                               (implicit hc: HeaderCarrier): Future[HttpResult[EmailVerificationState]] =
    http.POST[JsObject, HttpResult[EmailVerificationState]](checkVerifiedEmailUrl, Json.obj("email" -> emailAddress))

  def createEmailVerificationRequest(emailAddress: String, continueUrl: String)
                                    (implicit hc: HeaderCarrier): Future[HttpResult[EmailVerificationRequest]] = {
    val jsonBody =
      Json.obj(
        "email" -> emailAddress,
        "templateId" -> "verifyEmailAddress",
        "templateParameters" -> Json.obj(),
        "linkExpiryDuration" -> "P1D",
        "continueUrl" -> continueUrl
      )

    http.POST[JsObject, HttpResult[EmailVerificationRequest]](createEmailVerificationRequestUrl, jsonBody)
  }



  private val emailPinVerificationUrl: String = s"${appConfig.emailVerificationBaseUrl}/email-verification/request-passcode"

  def requestEmailPasscode(emailAddress: String)
                          (implicit hc: HeaderCarrier): Future[HttpResult[EmailVerificationPasscodeRequest]] = {
    val jsonBody =
      Json.obj(
        "email" -> emailAddress,
        "serviceName" -> "HMRC VAT"
      )

    http.POST[JsObject, HttpResult[EmailVerificationPasscodeRequest]](emailPinVerificationUrl, jsonBody)
  }

}
