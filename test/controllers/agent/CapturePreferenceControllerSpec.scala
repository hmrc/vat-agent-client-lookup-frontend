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

import assets.BaseTestConstants._
import audit.mocks.MockAuditingService
import audit.models.YesPreferenceAttemptedAuditModel
import common.SessionKeys
import controllers.ControllerBaseSpec
import forms.PreferenceForm.yes
import models.Agent
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterAll
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.Helpers._
import views.html.agent.CapturePreferenceView

class CapturePreferenceControllerSpec extends ControllerBaseSpec with MockAuditingService with BeforeAndAfterAll {

  val testRedirectUrl: String     = "/manage-vat-account"
  val defaultRedirectUrl: String  = "/vat-through-software/representative/client-vat-account"
  val testValidEmail: String      = "test@example.com"
  val testInvalidEmail: String    = "invalidEmail"
  val testYesPreference: String   = "yes"
  val testNoPreference: String    = "no"

  def target: CapturePreferenceController = new CapturePreferenceController(
    mockAgentOnlyAuthPredicate,
    mockPreferencePredicate,
    mockAuditingService,
    mcc,
    inject[CapturePreferenceView]
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "client VRN is in session" when {

        "a redirectUrl is in session and another is passed through in the 'show' method" when{

          "the user has an email preference" should {

            lazy val result = {
              target.show("newRedirectUrl")(request.withSession(
                SessionKeys.clientVRN -> "999999999",
                SessionKeys.redirectUrl -> testRedirectUrl,
                SessionKeys.preference -> yes
              ))
            }

            "return 200" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.OK
            }

            "render CapturePreferenceView page" in {
              messages(Jsoup.parse(contentAsString(result)).select("h1").text()) shouldBe "We no longer send confirmation letters in the post"
            }

            "store the new redirectUrl in session" in {
              session(result).get(SessionKeys.redirectUrl) shouldBe Some("newRedirectUrl")
            }
          }

          "the user does not have an email preference" should {

            lazy val result = {
              target.show("newRedirectUrl")(request.withSession(
                SessionKeys.clientVRN -> "999999999",
                SessionKeys.redirectUrl -> testRedirectUrl
              ))
            }

            "return 200" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.OK
            }

            "render CapturePreferenceView page" in {
              messages(Jsoup.parse(contentAsString(result)).select("h1").text()) shouldBe "We no longer send confirmation letters in the post"
            }

            "store the new redirectUrl in session" in {
              session(result).get(SessionKeys.redirectUrl) shouldBe Some("newRedirectUrl")
            }
          }
        }
      }

      "client VRN is not in session" when {

        "redirect URL is in session" should {

          lazy val result = {
            target.show()(request.withSession(
              SessionKeys.redirectUrl -> testRedirectUrl
            ))
          }

          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${controllers.agent.routes.SelectClientVrnController.show(testRedirectUrl).url}" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show(testRedirectUrl).url)
          }
        }

        "redirect URL is not in session" should {

          lazy val result = {
            target.show()(request)
          }

          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${controllers.agent.routes.SelectClientVrnController.show().url}" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show(defaultRedirectUrl).url)
          }
        }
      }
    }

    "a user does not have a valid enrolment" should {

      lazy val result = target.show()(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 403" in {
        mockAgentWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = target.submit(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 401" in {
        mockMissingBearerToken()
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "Calling the submit action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user enters the 'No' option" when {

        "redirect URL is in session" should {

          lazy val testRequest = Agent[AnyContentAsFormUrlEncoded](arn)(request
            .withSession(SessionKeys.redirectUrl -> testRedirectUrl)
            .withFormUrlEncodedBody("yes_no" -> testNoPreference)
          )

          lazy val result = {
            target.submit(testRequest)
          }

          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to redirect URL" in {
            redirectLocation(result) shouldBe Some(testRedirectUrl)
          }

          "add the preference to the session" in {
            session(result).get(SessionKeys.preference) shouldBe Some(testNoPreference)
          }
        }

        "redirect URL is not in session" should {

          lazy val testRequest = Agent[AnyContentAsFormUrlEncoded](arn)(request
            .withFormUrlEncodedBody("yes_no" -> testNoPreference)
          )

          lazy val result = {
            target.submit(testRequest)
          }

          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the change VAT details page" in {
            redirectLocation(result) shouldBe Some("/customer-details")
          }

          "add the preference to the session" in {
            session(result).get(SessionKeys.preference) shouldBe Some(testNoPreference)
          }
        }
      }

      "the user enters the 'Yes' option and an email address" should {

        lazy val testRequest =
          Agent[AnyContentAsFormUrlEncoded](arn)(request
            .withSession(SessionKeys.redirectUrl -> testRedirectUrl)
            .withFormUrlEncodedBody("yes_no" -> testYesPreference, "email" -> testValidEmail))
        lazy val result = target.submit(testRequest)

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the confirm email controller" in {
          redirectLocation(result) shouldBe Some(controllers.agent.routes.ConfirmEmailController.show.url)
        }

        "add the preference to the session" in {
          session(result).get(SessionKeys.preference) shouldBe Some(testYesPreference)
        }

        "add the provided email address to the session" in {
          session(result).get(SessionKeys.notificationsEmail) shouldBe Some(testValidEmail)
        }

        "audit the event" in {
          mockAgentAuthorised()
          await(target.submit(testRequest))
          verifyExtendedAudit(
            YesPreferenceAttemptedAuditModel(arn, testValidEmail),
            Some(controllers.agent.routes.CapturePreferenceController.submit.url)
          )
        }
      }

      "the user enters invalid data" should {

        lazy val result = target.submit(request
          .withSession(SessionKeys.redirectUrl -> testRedirectUrl)
          .withFormUrlEncodedBody("yes_no" -> testYesPreference, "email" -> testInvalidEmail))

        "reload the page with errors" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.BAD_REQUEST
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user does not have a valid enrolment" should {

      lazy val result = target.submit(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 403" in {
        mockAgentWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = target.submit(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 401" in {
        mockMissingBearerToken()
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
