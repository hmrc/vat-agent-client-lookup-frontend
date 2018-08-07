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

package controllers

import config.ErrorHandler
import mocks.MockAuth
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class JourneySetupControllerSpec extends ControllerBaseSpec with MockAuth {

  object TestJourneySetupControllerSpec extends JourneySetupController(
    messagesApi,
    mockAgentOnlyAuthPredicate,
    app.injector.instanceOf[ErrorHandler],
    mockConfig
  )

  val goodRedirectUrl = "http://localhost:/www.test.com"

  "Posting a valid url to the journeySetup action" when {

    "the user has a valid Agent enrolment" should {

      "return 200 when the url is relative to the host" in {

        val js:JsValue = Json.parse(s"""{"redirectUrl" : "$goodRedirectUrl"}""")
        lazy val request = FakeRequest("POST", "/").withJsonBody(js)
        lazy val result = TestJourneySetupControllerSpec.journeySetup()(request)
        mockAgentAuthorised()
        status(result) shouldBe Status.OK

      }

      "return 400 when the url is empty" in {

        val js:JsValue = Json.parse(s"""{"redirectUrl" : ""}""")
        lazy val request = FakeRequest("POST", "/").withJsonBody(js)
        lazy val result = TestJourneySetupControllerSpec.journeySetup()(request)
        mockAgentAuthorised()
        status(result) shouldBe Status.BAD_REQUEST

      }

      "return 400 when the url is not valid" in {

        val js:JsValue = Json.parse(s"""{"redirectUrl" : "www.google.com"}""")
        lazy val request = FakeRequest("POST", "/").withJsonBody(js)
        lazy val result = TestJourneySetupControllerSpec.journeySetup()(request)
        mockAgentAuthorised()
        status(result) shouldBe Status.BAD_REQUEST

      }

      "store the redirect url in session" in {

        mockAgentAuthorised()

        val result = await(TestJourneySetupControllerSpec.journeySetup()(fakeRequestWithJson))
        val sessionUrl = await(result map {

          res => {
            session(res).get(common.SessionKeys.redirectUrl).get
          }

        })

        sessionUrl shouldEqual goodRedirectUrl

      }
    }

    "the user doesn't have a valid Agent enrolment" should {

      "return 403 Forbidden" in {

        val js = Json.obj(
          common.SessionKeys.redirectUrl -> "http://localhost:/www.test.com")
        lazy val request = FakeRequest("POST", "/").withJsonBody(js)
        lazy val result = TestJourneySetupControllerSpec.journeySetup()(request)
        mockUnauthorised()
        status(result) shouldBe Status.FORBIDDEN
      }

    }
  }

  "Calling extractRedirectUrl" when {

    "a valid JsValue has been provided" should {

      "return a url" in {
        val js:JsValue = Json.parse("""{"redirectUrl" : "/exampleUrl"}""")
        TestJourneySetupControllerSpec.extractRedirectUrl(js) shouldBe Some("/exampleUrl")
      }

      "return an empty string when empty redirect url provided" in {
        val js:JsValue = Json.parse("""{"redirectUrl" : ""}""")
        TestJourneySetupControllerSpec.extractRedirectUrl(js) shouldBe Some("")
      }

      "return an empty string when an incorrect json key is provided" in {
        val js:JsValue = Json.parse("""{"dog" : "www.test.com"}""")
        TestJourneySetupControllerSpec.extractRedirectUrl(js) shouldBe None
      }
    }
  }
}
