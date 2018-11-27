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
import play.api.http.Status
import play.api.test.Helpers._

class CapturePreferenceControllerSpec extends ControllerBaseSpec {

  val testRedirectUrl: String     = "/manage-vat-account"
  val testValidEmail: String      = "test@example.com"
  val testInvalidEmail: String    = "invalidEmail"
  val testYesPreference: String   = "yes"
  val testNoPreference: String    = "no"

  def target: CapturePreferenceController = new CapturePreferenceController(
    messagesApi,
    mockAgentOnlyAuthPredicate,
    mockConfig
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" should {

      lazy val result = target.show(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 200" in {
        mockAgentAuthorised()
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = target.show(request.withSession(
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

      "the user enters the 'No' option" should {

        lazy val result = target.submit(request
          .withSession(SessionKeys.redirectUrl -> testRedirectUrl)
          .withFormUrlEncodedBody("yes_no" -> testNoPreference))

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the select client VRN view" in {
          redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show().url)
        }

        "add the preference to the session" in {
          session(result).get(SessionKeys.preference) shouldBe Some(testNoPreference)
        }
      }

      "the user enters the 'Yes' option and an email address" should {

        lazy val result = target.submit(request
          .withSession(SessionKeys.redirectUrl -> testRedirectUrl)
          .withFormUrlEncodedBody("yes_no" -> testYesPreference, "email" -> testValidEmail))

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the select client VRN view" in {
          redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show().url)
        }

        "add the preference to the session" in {
          session(result).get(SessionKeys.preference) shouldBe Some(testYesPreference)
        }

        "add the new email to the session" in {
          session(result).get(SessionKeys.email) shouldBe Some(testValidEmail)
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
