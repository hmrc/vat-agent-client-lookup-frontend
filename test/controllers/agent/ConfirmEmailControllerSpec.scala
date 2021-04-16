/*
 * Copyright 2021 HM Revenue & Customs
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

import assets.BaseTestConstants.arn
import audit.mocks.MockAuditingService
import audit.models.YesPreferenceVerifiedAuditModel
import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.services.MockEmailVerificationService
import models.Agent
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterAll
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._
import views.html.agent.CheckYourAnswersView

import scala.concurrent.Future

class ConfirmEmailControllerSpec extends ControllerBaseSpec with MockEmailVerificationService
  with MockAuditingService with BeforeAndAfterAll {

  object TestConfirmEmailController extends ConfirmEmailController(
    mockAgentOnlyAuthPredicate,
    mockPreferencePredicate,
    mockEmailVerificationService,
    mockErrorHandler,
    mockAuditingService,
    mcc,
    inject[CheckYourAnswersView],
    ec,
    mockConfig
  )

  val testEmail: String = "test@email.co.uk"

  "Calling the show action in ConfirmEmailController" when {

    "there is an email in session" should {
      lazy val testRequest = request.withSession(SessionKeys.notificationsEmail -> testEmail)
      lazy val result = {
        TestConfirmEmailController.show(testRequest)
      }

      "return 200" in {
        mockAgentAuthorised()
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't an email in session" should {
      lazy val result = {
        TestConfirmEmailController.show(request)
      }

      "return 303" in {
        mockAgentAuthorised()
        status(result) shouldBe Status.SEE_OTHER
      }

      "take the user to enter a new email address" in {
        redirectLocation(result) shouldBe Some("/vat-through-software/representative/email-notification")
      }
    }

    "the user is not authorised" should {
      lazy val testRequest = request.withSession(SessionKeys.notificationsEmail -> testEmail)
      lazy val result = {
        TestConfirmEmailController.show(testRequest)
      }
      lazy val document = Jsoup.parse(bodyOf(result))

      "return 500" in {
        mockUnauthorised()
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "show the technical difficulties page" in {
        document.title shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }
  }

  "Calling the isEmailVerified action in ConfirmEmailController" when {
    "there is no redirectUrl in session" when {
      "there is an email in session and it's verified" should {

        lazy val testRequest =
          Agent[AnyContentAsEmpty.type](arn)(request.withSession(SessionKeys.notificationsEmail -> testEmail))
        lazy val result = {
          mockGetEmailVerificationState(testEmail)(Future(Some(true)))
          TestConfirmEmailController.isEmailVerified()(testRequest)
        }

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to change business details in manage vat subscription frontend" in {
          redirectLocation(result) shouldBe
            Some("/customer-details")
        }

        "audit the event" in {
          mockAgentAuthorised()
          mockGetEmailVerificationState(testEmail)(Future(Some(true)))
          await(TestConfirmEmailController.isEmailVerified()(testRequest))
          verifyExtendedAudit(
            YesPreferenceVerifiedAuditModel(arn, testEmail),
            Some(controllers.agent.routes.ConfirmEmailController.isEmailVerified().url)
          )
        }
      }
    }

    "there is a redirect url in session" should {
      lazy val testRequest =
        Agent[AnyContentAsEmpty.type](arn)(request.withSession(
          SessionKeys.notificationsEmail -> testEmail,
          SessionKeys.redirectUrl -> "/this-is-a-random-FIRIN-MUH-LASER-BWAAAAAAAAAAH"
        ))
      lazy val result = {
        mockGetEmailVerificationState(testEmail)(Future(Some(true)))
        TestConfirmEmailController.isEmailVerified()(testRequest)
      }

      "return 303" in {
        mockAgentAuthorised()
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to select client VRN page" in {
        redirectLocation(result) shouldBe
          Some("/this-is-a-random-FIRIN-MUH-LASER-BWAAAAAAAAAAH")
      }

      "audit the event" in {
        mockAgentAuthorised()
        mockGetEmailVerificationState(testEmail)(Future(Some(true)))
        await(TestConfirmEmailController.isEmailVerified()(testRequest))
        verifyExtendedAudit(
          YesPreferenceVerifiedAuditModel(arn, testEmail),
          Some(controllers.agent.routes.ConfirmEmailController.isEmailVerified().url)
        )
      }
    }

    "there is a non-verified email in session" when {

      "the useEmailPinVerification feature switch is enabled" should {
        lazy val testRequest = request.withSession(SessionKeys.notificationsEmail -> testEmail)
        lazy val result = {
          mockGetEmailVerificationState(testEmail)(Future(Some(false)))
          TestConfirmEmailController.isEmailVerified()(testRequest)
        }

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the send email verification page" in {
          redirectLocation(result) shouldBe Some("/vat-through-software/representative/send-passcode")
        }
      }
    }

    "there isn't an email in session" should {
      lazy val result = {
        TestConfirmEmailController.isEmailVerified()(request)
      }

      "return 303" in {
        mockAgentAuthorised()
        status(result) shouldBe Status.SEE_OTHER
      }

      "take the user to the capture email address page" in {
        redirectLocation(result) shouldBe Some("/vat-through-software/representative/email-notification")
      }
    }

    "the user is not authorised" should {
      lazy val testRequest = request.withSession(SessionKeys.notificationsEmail -> testEmail)
      lazy val result = {
        TestConfirmEmailController.isEmailVerified()(testRequest)
      }
      lazy val document = Jsoup.parse(bodyOf(result))

      "return 500" in {
        mockUnauthorised()
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "show the technical difficulties page" in {
        document.title shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }
  }
}
