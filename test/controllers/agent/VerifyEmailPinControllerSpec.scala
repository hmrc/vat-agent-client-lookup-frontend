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

package controllers.agent

import common.SessionKeys
import connectors.httpParsers.VerifyPasscodeHttpParser.{AlreadyVerified, IncorrectPasscode, PasscodeNotFound, SuccessfullyVerified, TooManyAttempts}
import controllers.ControllerBaseSpec
import mocks.services.MockEmailVerificationService
import models.errors.UnexpectedError
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterAll
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.agent.VerifyEmailPinView
import views.html.errors.agent.IncorrectPasscodeView

class VerifyEmailPinControllerSpec extends ControllerBaseSpec with BeforeAndAfterAll with MockEmailVerificationService {

  object TestVerifyEmailPinController extends VerifyEmailPinController(
    mockEmailVerificationService,
    mockAgentOnlyAuthPredicate,
    mockPreferencePredicate,
    mockErrorHandler,
    mcc,
    inject[VerifyEmailPinView],
    inject[IncorrectPasscodeView]
  )

  val testEmail: String = "test@email.co.uk"

  lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/email-enter-code")
  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/email-enter-code")
  lazy val testPasscodeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/send-passcode")

  "Calling the show action in VerifyEmailPinController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" should {

        "show the Confirmation Email page" in {

          mockAgentAuthorised()

          val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
          val result = {
            TestVerifyEmailPinController.show(request)
          }

          status(result) shouldBe Status.OK
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }

      "there isn't an email in session" should {

        "redirect to the capture email page" in {

          mockAgentAuthorised()

          val result = {
            TestVerifyEmailPinController.show(request)
          }

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CapturePreferenceController.show().url)
        }
      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailPinController.show(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(contentAsString(result)).title shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }
  }

  "Calling the submit action in VerifyEmailPinController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "there are no errors in the form" when {

          "the email verification service returns SuccessfullyVerified" should {

            "redirect the user to manage vat customer details" in {

              mockAgentAuthorised()

              val request = testPostRequest
                .withFormUrlEncodedBody(("passcode", "123456"))
                .withSession(SessionKeys.notificationsEmail -> testEmail)
              val result = {
                mockVerifyPasscodeRequest(Right(SuccessfullyVerified))
                TestVerifyEmailPinController.submit(request)
              }

              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some("/customer-details")
            }

          }

          "the email verification service returns AlreadyVerified" should {

            "redirect the user to manage vat customer details" in {

              mockAgentAuthorised()

              val request = testPostRequest
                .withFormUrlEncodedBody(("passcode", "123456"))
                .withSession(SessionKeys.notificationsEmail -> testEmail)
              val result = {
                mockVerifyPasscodeRequest(Right(AlreadyVerified))
                TestVerifyEmailPinController.submit(request)
              }

              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some("/customer-details")
            }
          }

          "the email verification service returns too many attempts" should {

            "return 400" in {

              mockAgentAuthorised()

              val request = testPostRequest
                .withFormUrlEncodedBody(("passcode", "123456"))
                .withSession(SessionKeys.notificationsEmail -> testEmail)
              lazy val result = {
                mockVerifyPasscodeRequest(Right(TooManyAttempts))
                TestVerifyEmailPinController.submit(request)
              }

              status(result) shouldBe BAD_REQUEST
            }
          }

          "the email verification service returns PasscodeNotFound" should {

            "return 400" in {

              mockAgentAuthorised()

              val request = testPostRequest
                .withFormUrlEncodedBody(("passcode", "123456"))
                .withSession(SessionKeys.notificationsEmail -> testEmail)
              lazy val result = {
                mockVerifyPasscodeRequest(Right(PasscodeNotFound))
                TestVerifyEmailPinController.submit(request)
              }

              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns IncorrectPasscode" should {

            "return 400" in {

              mockAgentAuthorised()

              val request = testPostRequest
                .withFormUrlEncodedBody(("passcode", "123456"))
                .withSession(SessionKeys.notificationsEmail -> testEmail)
              lazy val result = {
                mockVerifyPasscodeRequest(Right(IncorrectPasscode))
                TestVerifyEmailPinController.submit(request)
              }

              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns an unexpected error" should {

            "return 500" in {
              val request = testPostRequest
                .withFormUrlEncodedBody(("passcode", "123456"))
                .withSession(SessionKeys.notificationsEmail -> testEmail)
              lazy val result = {
                mockVerifyPasscodeRequest(Left(UnexpectedError(Status.INTERNAL_SERVER_ERROR, "Err0r")))
                TestVerifyEmailPinController.submit(request)
              }
              mockAgentAuthorised()
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }
        }

        "there are errors in the form" should {

          "return a bad request" in {

            mockAgentAuthorised()

            val request = testPostRequest
              .withFormUrlEncodedBody(("passcode", "badthings"))
              .withSession(SessionKeys.notificationsEmail -> testEmail)
            val result = {
              TestVerifyEmailPinController.submit(request)
            }

            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "there isn't an email in session" should {

        "redirect to the capture email page" in {

          mockAgentAuthorised()

          val result = {
            TestVerifyEmailPinController.submit(request)
          }

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CapturePreferenceController.show().url)
        }
      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testPostRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailPinController.submit(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(contentAsString(result)).title shouldBe "There is a problem with the service - " +
          "Your client’s VAT details - GOV.UK"
      }
    }

  }

  "Calling the requestPasscode action in VerifyEmailPinController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "the EmailPasscodeRequest returns 'true'" should {

          "redirect the user to manage vat customer details" in {

            mockAgentAuthorised()

            val request = testPasscodeRequest
              .withSession(SessionKeys.notificationsEmail -> testEmail)
            val result = {
              mockCreatePasscodeRequest(Some(true))
              TestVerifyEmailPinController.requestPasscode(request)
            }

            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/vat-through-software/representative/email-enter-code")
          }
        }

        "the EmailPasscodeRequest returns 'false'" should {

          "redirect the user to manage vat customer details" in {

            mockAgentAuthorised()

            val request = testPasscodeRequest
              .withSession(SessionKeys.notificationsEmail -> testEmail)
            val result = {
              mockCreatePasscodeRequest(Some(false))
              TestVerifyEmailPinController.requestPasscode(request)
            }

            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/customer-details")
          }
        }

        "the EmailPasscodeRequest returns an error" should {

          "redirect the user to manage vat customer details" in {

            mockAgentAuthorised()

            val request = testPasscodeRequest
              .withSession(SessionKeys.notificationsEmail -> testEmail)
            val result = {
              mockCreatePasscodeRequest(None)
              TestVerifyEmailPinController.requestPasscode(request)
            }

            status(result) shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "there is no email in session" should {

        "redirect to the capture email page" in {

          mockAgentAuthorised()

          val result = {
            TestVerifyEmailPinController.requestPasscode(request)
          }

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CapturePreferenceController.show().url)
        }
      }
    }
  }

}