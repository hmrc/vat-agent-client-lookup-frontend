/*
 * Copyright 2018 HM Revenue & Customs
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
import models.Agent
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}

class VerifyEmailControllerSpec extends ControllerBaseSpec with MockEmailVerificationService {

  object TestVerifyEmailController extends VerifyEmailController(
    mockAgentOnlyAuthPredicate,
    mockPreferencePredicate,
    messagesApi,
    mockEmailVerificationService,
    mockErrorHandler,
    mockConfig
  )

  val testEmail: String = "test@email.co.uk"

  lazy val testSendEmailRequest = FakeRequest("GET", "/send-verification")
  lazy val testGetRequest = FakeRequest("GET", "/verify-email-address")

  "Calling the extractSessionEmail function in VerifyEmailController" when {

    "there is an authenticated request from a user with an email in session" should {
      "result in an email address being retrieved if there is an email" in {

        mockAgentAuthorised()

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = testGetRequest.withSession(
          SessionKeys.notificationsEmail -> testEmail)

        val agent = Agent[AnyContent](Enrolments(Set(new Enrolment("HMRC-AS-AGENT",
          Seq(EnrolmentIdentifier("AgentReferenceNumber", "XAIT00000000")), "Activated", None))))(request)

        TestVerifyEmailController.extractSessionEmail(agent) shouldBe Some(testEmail)
      }
    }
  }

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

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> "")
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
        Jsoup.parse(bodyOf(result)).title shouldBe "There is a problem with the service - VAT reporting through software - GOV.UK"
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
      "redirect to the select client VRN page" in {

        mockAgentAuthorised()
        mockCreateEmailVerificationRequest(Some(false))

        val request = testSendEmailRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestVerifyEmailController.sendVerification(request)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.SelectClientVrnController.show().url)
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

        val request = testSendEmailRequest.withSession(SessionKeys.notificationsEmail -> "")
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
          "VAT reporting through software - GOV.UK"
      }
    }

  }
}