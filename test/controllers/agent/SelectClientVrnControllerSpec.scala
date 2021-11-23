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

import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.MockAuth
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.agent.SelectClientVrnView

class SelectClientVrnControllerSpec extends ControllerBaseSpec with MockAuth with BeforeAndAfterEach {

  object TestClientVrnController extends SelectClientVrnController(
    mockAgentOnlyAuthPredicate,
    mcc,
    inject[SelectClientVrnView]
  )

  "Calling the .show() action" when {

    val testRedirectUrl = "/manage-vat-account"

    "redirect URL is supplied" should {

      lazy val result = {
        TestClientVrnController.show(testRedirectUrl)(request)
      }

      "return 200" in {
        mockAgentAuthorised()
        status(result) shouldBe Status.OK
      }

      "render SelectClientVrnView page" in {
        messages(Jsoup.parse(contentAsString(result)).select("h1").text) shouldBe "What is your client’s VAT number?"
      }

      "add redirectURL to session" in {
        session(result).get(SessionKeys.redirectUrl) shouldBe Some(testRedirectUrl)
      }
    }

    "redirect URL is not supplied" should {

      lazy val result = {
        TestClientVrnController.show("")(request)
      }

      "return 200" in {
        mockAgentAuthorised()
        status(result) shouldBe Status.OK
      }

      "render SelectClientVrnView page" in {
        messages(Jsoup.parse(contentAsString(result)).select("h1").text) shouldBe "What is your client’s VAT number?"
      }

      "add the what to do redirect url" in {
        session(result).get(SessionKeys.redirectUrl) shouldBe None
      }
    }
  }

  "Calling the .submit action" when {

    "the user is an authorised Agent" when {

      "valid data is posted" should {

        lazy val request = FakeRequest("POST", "/")
          .withFormUrlEncodedBody(("vrn", "999969202"))
          .withSession(SessionKeys.clientMandationStatus -> "Non MTDfB")

        "original request should contain mandation status in cookie" in {
          request.session.get(SessionKeys.clientMandationStatus).get shouldBe "Non MTDfB"
        }

        lazy val result = TestClientVrnController.submit(request)

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "contain the correct location header" in {
          redirectLocation(result) shouldBe Some(controllers.agent.routes.ConfirmClientVrnController.show.url)
        }

        "add Client VRN to session cookie" in {
          session(result).get(SessionKeys.clientVRNDeprecated).get shouldBe "999969202"
          session(result).get(SessionKeys.clientVRN).get shouldBe "999969202"
        }

        "remove mandation status from session cookie" in {
          session(result).get(SessionKeys.clientMandationStatus) shouldBe None
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
