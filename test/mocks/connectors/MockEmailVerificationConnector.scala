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

package mocks.connectors

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.{reset, when}
import connectors.EmailVerificationConnector
import uk.gov.hmrc.http.HeaderCarrier
import connectors.httpParsers.CreateEmailVerificationRequestHttpParser.EmailVerificationRequest
import connectors.httpParsers.GetEmailVerificationStateHttpParser.EmailVerificationState
import connectors.httpParsers.RequestPasscodeHttpParser.EmailVerificationPasscodeRequest
import connectors.httpParsers.ResponseHttpParser.HttpResult
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future

trait MockEmailVerificationConnector extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockEmailVerificationConnector)
  }

  val mockEmailVerificationConnector: EmailVerificationConnector = mock[EmailVerificationConnector]

  def mockGetEmailVerificationState(response: Future[HttpResult[EmailVerificationState]]): Unit =
    when(mockEmailVerificationConnector.getEmailVerificationState(any[String])(any[HeaderCarrier])) thenReturn response

  def mockCreateEmailVerificationRequest(response: Future[HttpResult[EmailVerificationRequest]]): Unit =
    when(mockEmailVerificationConnector.createEmailVerificationRequest(any[String], any[String])
                                                                      (any[HeaderCarrier])) thenReturn response

  def mockRequestEmailPasscode(response: Future[HttpResult[EmailVerificationPasscodeRequest]]): Unit =
    when(mockEmailVerificationConnector.requestEmailPasscode(any[String], any[String])(any[HeaderCarrier])) thenReturn response
}
