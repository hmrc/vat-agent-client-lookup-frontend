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
import mocks.MockAuth
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SelectClientVrnControllerSpec extends ControllerBaseSpec with MockAuth {

  object TestClientVrnController extends SelectClientVrnController(
    messagesApi,
    mockAgentOnlyAuthPredicate,
    serviceErrorHandler,
    mockConfig
  )

  "Calling the .show() action" when {

    val testRedirectUrl   = "/manage-vat-account"
    val testYesPreference = "yes"
    val testNoPreference  = "no"
    val testEmail         = "test@example.com"

    "the user has a preference of 'yes' in session" when {

      "the user has a verified email in session" when {

        "a valid redirect URL is provided" when {

          "there is a redirect URL currently in session" should {

            lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
              SessionKeys.redirectUrl -> testRedirectUrl,
              SessionKeys.preference -> testYesPreference,
              SessionKeys.verifiedAgentEmail -> testEmail
            ))

            "return 200" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "not add the requested redirect URL to the session" in {
              session(result).get(SessionKeys.redirectUrl) shouldBe None
            }
          }

          "there is no redirect URL currently in session" should {

            lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
              SessionKeys.preference -> testYesPreference,
              SessionKeys.verifiedAgentEmail -> testEmail
            ))

            "return 200" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "add the redirect URL to the session" in {
              session(result).get(SessionKeys.redirectUrl) shouldBe Some(testRedirectUrl)
            }
          }
        }

        "an invalid redirect URL is provided" when {

          "there is a redirect URL currently in session" should {

            lazy val result = TestClientVrnController.show("www.google.com")(request.withSession(
              SessionKeys.redirectUrl -> testRedirectUrl,
              SessionKeys.preference -> testYesPreference,
              SessionKeys.verifiedAgentEmail -> testEmail
            ))

            "return 200" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "not add the requested redirect URL to the session" in {
              session(result).get(SessionKeys.redirectUrl) shouldBe None
            }
          }

          "there is no redirect URL currently in session" should {

            lazy val result = TestClientVrnController.show("www.google.com")(request.withSession(
              SessionKeys.preference -> testYesPreference,
              SessionKeys.verifiedAgentEmail -> testEmail
            ))

            "return 500" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "not add the requested redirect URL to the session" in {
              session(result).get(SessionKeys.redirectUrl) shouldBe None
            }
          }
        }
      }

      "the user has no verified email in session" should {

        lazy val result = TestClientVrnController.show("www.google.com")(request.withSession(
          SessionKeys.preference -> testYesPreference
        ))

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the Capture Preference controller action" in {
          redirectLocation(result) shouldBe Some(controllers.agent.routes.CapturePreferenceController.show().url)
        }

        "not add the requested redirect URL to the session" in {
          session(result).get(SessionKeys.redirectUrl) shouldBe None
        }
      }
    }

    "the user has a preference of 'no' in session" when {

      "a valid redirect URL is provided" when {

        "there is a redirect URL currently in session" should {

          lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
            SessionKeys.redirectUrl -> testRedirectUrl,
            SessionKeys.preference  -> testNoPreference
          ))

          "return 200" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }

          "not add the requested redirect URL to the session" in {
            session(result).get(SessionKeys.redirectUrl) shouldBe None
          }
        }

        "there is no redirect URL currently in session" should {

          lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
            SessionKeys.preference -> testNoPreference
          ))

          "return 200" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }

          "add the redirect URL to the session" in {
            session(result).get(SessionKeys.redirectUrl) shouldBe Some(testRedirectUrl)
          }
        }
      }

      "an invalid redirect URL is provided" when {

        "there is a redirect URL currently in session" should {

          lazy val result = TestClientVrnController.show("www.google.com")(request.withSession(
            SessionKeys.redirectUrl -> testRedirectUrl,
            SessionKeys.preference  -> testNoPreference
          ))

          "return 200" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }

          "not add the requested redirect URL to the session" in {
            session(result).get(SessionKeys.redirectUrl) shouldBe None
          }
        }

        "there is no redirect URL currently in session" should {

          lazy val result = TestClientVrnController.show("www.google.com")(request.withSession(
            SessionKeys.preference  -> testNoPreference
          ))

          "return 500" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }

          "not add the requested redirect URL to the session" in {
            session(result).get(SessionKeys.redirectUrl) shouldBe None
          }
        }
      }
    }

    "the user does not have a preference in session" should {

      lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 303" in {
        mockAgentAuthorised()
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the Capture Preference controller action" in {
        redirectLocation(result) shouldBe Some(controllers.agent.routes.CapturePreferenceController.show().url)
      }

      "not add the requested redirect URL to the session" in {
        session(result).get(SessionKeys.redirectUrl) shouldBe None
      }
    }
  }

  "Calling the .submit action" when {

    "the user is an authorised Agent" when {

      "valid data is posted" should {

        lazy val request = FakeRequest("POST", "/").withFormUrlEncodedBody(("vrn", "999969202"))
        lazy val result = TestClientVrnController.submit(request)

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "contain the correct location header" in {
          redirectLocation(result) shouldBe Some(controllers.agent.routes.ConfirmClientVrnController.show().url)
        }

        "contain the Clients VRN in the session" in {
          session(result).get(SessionKeys.clientVRN) shouldBe Some("999969202")
        }
      }

      "invalid data is posted" should {

        lazy val request = FakeRequest("POST", "/").withFormUrlEncodedBody(("vrn", "123456789"))
        lazy val result = TestClientVrnController.submit(request)

        "return 400" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.BAD_REQUEST
        }
      }
    }
  }

  "The extractRedirectUrl() function" when {

    "a valid relative redirect URL is provided" should {

      "return the URL" in {
        val result = TestClientVrnController.extractRedirectUrl("/homepage")
        result shouldBe Some("/homepage")
      }
    }

    "a valid absolute redirect URL is provided" should {

      "return the URL" in {
        val result = TestClientVrnController.extractRedirectUrl("http://localhost:9149/homepage")
        result shouldBe Some("http://localhost:9149/homepage")
      }
    }

    "an invalid redirect URL is provided" should {

      "return None" in {
        val result = TestClientVrnController.extractRedirectUrl("http://www.google.com")
        result shouldBe None
      }
    }

    "an exception is thrown when trying to construct a continue URL" should {

      "return None" in {
        val result = TestClientVrnController.extractRedirectUrl("99")
        result shouldBe None
      }
    }
  }
}
