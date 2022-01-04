/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import connectors.httpParsers.CreateEmailVerificationRequestHttpParser.{EmailAlreadyVerified, EmailVerificationRequestSent}
import connectors.httpParsers.GetEmailVerificationStateHttpParser.{EmailNotVerified, EmailVerified}
import connectors.httpParsers.RequestPasscodeHttpParser.{EmailIsAlreadyVerified, EmailVerificationPasscodeRequestSent}
import mocks.connectors.MockEmailVerificationConnector
import models.errors.UnexpectedError
import org.mockito.Mockito.{never, verify}
import play.api.http.Status._
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.TestUtil

import scala.concurrent.Future

class EmailVerificationServiceSpec extends TestUtil with MockEmailVerificationConnector {

  object TestStoreEmailService extends EmailVerificationService(
    mockEmailVerificationConnector,
    mockConfig
  )

  val continueUrl = "/continue"
  val testEmail: String = "pepsimac666@hotmail.co.uk"

  "Creating an email verification request" when {

    "the email verification feature switch is on" when {

      "the email verification request is sent successfully" should {

        "return Some(true)" in {

          mockCreateEmailVerificationRequest(Future.successful(Right(EmailVerificationRequestSent)))
          val res: Option[Boolean] = {
            mockConfig.features.emailVerificationEnabled(true)
            await(TestStoreEmailService.createEmailVerificationRequest(testEmail, continueUrl))
          }
          res shouldBe Some(true)
        }
      }

      "the email address has already been verified" should {

        "return Some(false)" in {

          mockCreateEmailVerificationRequest(Future.successful(Right(EmailAlreadyVerified)))
          val res: Option[Boolean] = {
            mockConfig.features.emailVerificationEnabled(true)
            await(TestStoreEmailService.createEmailVerificationRequest(testEmail, continueUrl))
          }
          res shouldBe Some(false)
        }
      }

      "the email address verification request fails" should {

        "return None" in {

          mockCreateEmailVerificationRequest(Future.successful(Left(UnexpectedError(BAD_REQUEST, ""))))
          val res: Option[Boolean] = {
            mockConfig.features.emailVerificationEnabled(true)
            await(TestStoreEmailService.createEmailVerificationRequest(testEmail, continueUrl))
          }
          res shouldBe None
        }
      }
    }

    "the email verification feature switch is off" should {

      def res: Option[Boolean] = {
        mockConfig.features.emailVerificationEnabled(false)
        await(TestStoreEmailService.createEmailVerificationRequest(testEmail, "/continue"))
      }

      "return Some(false)" in {
        res shouldBe Some(false)
      }

      "not call the email verification connector" in {
        res
        verify(mockEmailVerificationConnector, never()).createEmailVerificationRequest(testEmail, "/continue")
      }
    }
  }


  "Checking email verification status" when {

    "the email verification feature switch is on" when {

      "the email is verified" should {

        "return Some(true)" in {

          mockGetEmailVerificationState(Future.successful(Right(EmailVerified)))
          val res: Option[Boolean] = {
            mockConfig.features.emailVerificationEnabled(true)
            await(TestStoreEmailService.isEmailVerified(testEmail))
          }
          res shouldBe Some(true)
        }
      }

      "the email is not verified" should {

        "return Some(false)" in {

          mockGetEmailVerificationState(Future.successful(Right(EmailNotVerified)))
          val res: Option[Boolean] = {
            mockConfig.features.emailVerificationEnabled(true)
            await(TestStoreEmailService.isEmailVerified(testEmail))
          }
          res shouldBe Some(false)
        }
      }

      "the email verification status check fails" should {

        "return None" in {

          mockGetEmailVerificationState(Future.successful(Left(UnexpectedError(BAD_REQUEST, ""))))
          val res: Option[Boolean] = {
            mockConfig.features.emailVerificationEnabled(true)
            await(TestStoreEmailService.isEmailVerified(testEmail))
          }
          res shouldBe None
        }
      }
    }
  }

  "the email verification feature switch is off" should {

    def res: Option[Boolean] = {
      mockConfig.features.emailVerificationEnabled(false)
      await(TestStoreEmailService.isEmailVerified(testEmail))
    }

    "return Some(true)" in {
      res shouldBe Some(true)
    }

    "not call the email verification connector" in {
      res
      verify(mockEmailVerificationConnector, never()).getEmailVerificationState(testEmail)
    }
  }

  "Creating an email verification passcode request" when {

    "the emailPinVerificationEnabled feature switch is on" when {

      "the email verification passcode request is sent successfully" should {

        "return Some(true)" in {

          mockRequestEmailPasscode(Future.successful(Right(EmailVerificationPasscodeRequestSent)))
          val res: Option[Boolean] = {
            await(TestStoreEmailService.createEmailPasscodeRequest(testEmail, "en"))
          }
          res shouldBe Some(true)
        }
      }

      "the email address has already been verified" should {

        "return Some(false)" in {

          mockRequestEmailPasscode(Future.successful(Right(EmailIsAlreadyVerified)))
          val res: Option[Boolean] = {
            await(TestStoreEmailService.createEmailPasscodeRequest(testEmail, "en"))
          }
          res shouldBe Some(false)
        }
      }

      "the email verification passcode request fails" should {

        "return None" in {

          mockRequestEmailPasscode(Future.successful(Left(UnexpectedError(BAD_REQUEST, ""))))
          val res: Option[Boolean] = {
            await(TestStoreEmailService.createEmailPasscodeRequest(testEmail, "en"))
          }
          res shouldBe None
        }
      }
    }
  }
}
