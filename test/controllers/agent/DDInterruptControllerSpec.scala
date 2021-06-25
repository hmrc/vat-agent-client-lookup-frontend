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
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.agent.DirectDebitInterruptView

class DDInterruptControllerSpec extends ControllerBaseSpec {

  val controller = new DDInterruptController(mcc, mockAuthAsAgentWithClient, inject[DirectDebitInterruptView])
  lazy val ddSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    fakeRequestWithMtdVatAgentData.withSession(SessionKeys.viewedDDInterrupt -> "true")

  "The .submit action" when {

    "the user is authorised" when {

      "the DD interrupt feature switch is on" when {

        "the user has the DD interrupt session key" should {

          lazy val result = {
            mockAgentAuthorised()
            controller.submit(ddSessionRequest)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Agent Hub controller" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show().url)
          }
        }

        "the user does not have the DD interrupt session key" when {

          "the form binds successfully (the user ticked the checkbox)" should {

            lazy val result = {
              mockAgentAuthorised()
              controller.submit(fakeRequestWithMtdVatAgentData.withFormUrlEncodedBody(("checkbox", "true")))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the Agent Hub controller" in {
              redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show().url)
            }

            "add a value to the session to confirm the user has seen and accepted the terms of the DD interrupt" in {
              session(result).get(SessionKeys.viewedDDInterrupt) shouldBe Some("true")
            }
          }

          "there is a form error (the user did not tick the checkbox)" should {

            lazy val result = {
              mockAgentAuthorised()
              controller.submit(fakeRequestWithMtdVatAgentData.withFormUrlEncodedBody(("checkbox", "")))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }
          }
        }
      }

      "the DD interrupt feature switch is off" when {

        "the user has the DD interrupt session key" should {

          lazy val result = {
            mockConfig.features.directDebitInterruptFeature(false)
            mockAgentAuthorised()
            controller.submit(ddSessionRequest)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Agent Hub controller" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show().url)
          }
        }

        "the user does not have the DD interrupt session key" should {

          lazy val result = {
            mockConfig.features.directDebitInterruptFeature(false)
            mockAgentAuthorised()
            controller.submit(fakeRequestWithMtdVatAgentData)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Agent Hub controller" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show().url)
          }
        }
      }
    }

    "the user is unauthorised" should {

      lazy val result = {
        mockUnauthorised()
        controller.submit(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the Select Client VRN controller (no client VRN in session)" in {
        redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show().url)
      }
    }
  }
}