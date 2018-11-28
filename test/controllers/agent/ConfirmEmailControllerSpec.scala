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
import mocks.services.MockVatSubscriptionService
import models.Agent
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.{Waiters, _}
import play.api.http.Status
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}

class ConfirmEmailControllerSpec extends ControllerBaseSpec with MockVatSubscriptionService {

  object TestConfirmEmailController extends ConfirmEmailController(
    mockAgentOnlyAuthPredicate,
    messagesApi,
    mockVatSubscriptionService,
    mockErrorHandler,
    mockConfig
  )

  val testVatNumber: String = "999999999"
  val testEmail: String = "test@email.co.uk"

  lazy val testGetRequest = FakeRequest("GET", "/confirm-email")

  "Calling the extractEmail function in ConfirmEmailController" when {
    "there is an authenticated request from an Agent with an email in session" should {

      "result in an email address being retrieved if there is an email" in {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = testGetRequest.withSession(
         SessionKeys.notificationsEmail -> testEmail)

        val agent = Agent[AnyContent](Enrolments(Set(new Enrolment("HMRC-AS-AGENT",
          Seq(EnrolmentIdentifier("AgentReferenceNumber", "XAIT00000000")), "Activated", None))))(request)

        TestConfirmEmailController.extractSessionEmail(agent) shouldBe Some(testEmail)
      }
    }
  }

  "Calling the show action in ConfirmEmailController" when {
    "there is an email in session" should {

      "show the Confirm Email page" in {

        mockAgentAuthorised()

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestConfirmEmailController.show(request)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't an email in session" should {
      "take the user to enter a new email address" in {

        mockAgentAuthorised()

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> "")
        val result = TestConfirmEmailController.show(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/representative/email-notification")
      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestConfirmEmailController.show(request)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "There is a problem with the service - VAT reporting through software - GOV.UK"
      }
    }
  }

  "Calling the updateEmailAddress() action in ConfirmEmailController" when {

    "there is a verified email in session and the email has been updated successfully" should {
      "redirect to select client VRN page" in {

        mockAgentAuthorised()
        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)

        when(mockVatSubscriptionService.updateEmail(ArgumentMatchers.any(), ArgumentMatchers.any())
        (ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(true))

        val result = TestConfirmEmailController.updateEmailAddress()(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/representative/client-vat-number")

      }
    }

    "there is a non-verified email in session" should {
      "redirect the user to the send email verification pagee" in {

        mockAgentAuthorised()

        when(mockVatSubscriptionService.updateEmail(ArgumentMatchers.any(), ArgumentMatchers.any())
        (ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(false))

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestConfirmEmailController.updateEmailAddress()(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/representative/send-verification-request")

      }
    }

    "there is a verified email in session but email could not be updated because there was an error " +
      "trying to update the email address" should {
      "throw an Internal Server Exception" in {

        mockAgentAuthorised()

        when(mockVatSubscriptionService.updateEmail(ArgumentMatchers.any(), ArgumentMatchers.any())
        (ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(None)

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestConfirmEmailController.updateEmailAddress()(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe
          "There is a problem with the service - VAT reporting through software - GOV.UK"
      }
    }

    "there isn't an email in session" should {
      "take the user to the capture email address page" in {

        mockAgentAuthorised()

        val request = testGetRequest
        val result = TestConfirmEmailController.updateEmailAddress()(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/representative/email-notification")

      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testGetRequest.withSession(SessionKeys.notificationsEmail -> testEmail)
        val result = TestConfirmEmailController.updateEmailAddress()(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "There is a problem with the service - VAT reporting through software - GOV.UK"
      }
    }
  }
}
