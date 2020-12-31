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

package controllers.agent

import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.services.MockEmailVerificationService
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterAll
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.agent.VerifyEmailView

class VerifyEmailControllerSpec extends ControllerBaseSpec with MockEmailVerificationService with BeforeAndAfterAll {

  object TestVerifyEmailController extends VerifyEmailController(
    mockAgentOnlyAuthPredicate,
    mockPreferencePredicate,
    mockEmailVerificationService,
    mockErrorHandler,
    mcc,
    inject[VerifyEmailView],
    ec,
    mockConfig
  )

  val testEmail: String = "test@email.co.uk"

  lazy val testSendEmailRequest = FakeRequest("GET", "/send-verification")
  lazy val testGetRequest = FakeRequest("GET", "/verify-email-address")

  "Calling the show action in VerifyEmailController" when {

    "there is an email in session" should {
      "show the Confirmation Email page" in {

        mockAgentAuthorised()

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailController.show(request)

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't an email in session" should {
      "redirect to the capture email page" in {

        mockAgentAuthorised()

        val result = TestVerifyEmailController.show(request)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePreferenceController.show().url)
      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailController.show(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }
  }

  "Calling the sendVerification action in VerifyEmailController" when {

    "there is an email in session and the email request is successfully created" should {
      "redirect to the email verification page" in {

        mockAgentAuthorised()
        mockCreateEmailVerificationRequest(Some(true))

        val request = testSendEmailRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailController.sendVerification(request)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.VerifyEmailController.show().url)
      }
    }

    "there is an email in session and the email request is not created as already verified" should {
      "redirect to manage vat subscription" in {

        mockAgentAuthorised()
        mockCreateEmailVerificationRequest(Some(false))

        val request = testSendEmailRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailController.sendVerification(request)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("/customer-details")
      }
    }


    "there is an email in session and the email request returned an unexpected error" should {
      "show an internal server error" in {

        mockAgentAuthorised()
        mockCreateEmailVerificationRequest(None)

        val request = testSendEmailRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailController.sendVerification(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there is not an email in session and the email request returned an unexpected error" should {
      "show an internal server error" in {

        mockAgentAuthorised()
        mockCreateEmailVerificationRequest(None)

        val request = testSendEmailRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailController.sendVerification(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there isn't an email in session" should {
      "redirect to the capture email page" in {

        mockAgentAuthorised()

        val result = TestVerifyEmailController.sendVerification(request)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePreferenceController.show().url)
      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testSendEmailRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailController.sendVerification(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "There is a problem with the service - " +
          "Your client’s VAT details - GOV.UK"
      }
    }

    "the an email is in session" should {

      "redirect to the redirectUrl if one exists" in {
        mockAgentAuthorised()
        mockCreateEmailVerificationRequest(Some(false))

        val request = testSendEmailRequest.withSession(SessionKeys.notificationsEmail -> testEmail, SessionKeys.redirectUrl -> "/something")
        val result = TestVerifyEmailController.sendVerification(request)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("/something")
      }

      "redirect to the manageVat url if no redirect url exists" in {
        mockAgentAuthorised()
        mockCreateEmailVerificationRequest(Some(false))

        val request = testSendEmailRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailController.sendVerification(request)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(mockConfig.manageVatCustomerDetailsUrl)
      }
    }

  }
}