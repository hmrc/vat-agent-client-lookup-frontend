/*
 * Copyright 2019 HM Revenue & Customs
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

package pages.agent

import common.SessionKeys
import forms.ClientVrnForm
import helpers.IntegrationTestConstants._
import helpers.SessionCookieCrumbler
import models.agent.ClientVrnModel
import pages.BasePageISpec
import play.api.i18n.Messages
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class SelectClientVrnPageSpec extends BasePageISpec {

  val path = "/client-vat-number?redirectUrl=/homepage"

  "Calling the .show() action" when {

    def show: WSResponse = get(path)

    "the user is an Agent" when {

      "the Agent is signed up for HMRC-AS-AGENT (authorised)" when {

        "the agent has a preference of 'yes' and a verified email in session" should {

          "Render the Select a Client page" in {

            given.agent.isSignedUpToAgentServices

            When("I call the show Select Client VRN page")
            val res = get(path, formatPreference(Some("yes")) ++ formatVerifiedEmail(Some("test@example.com")))

            res should have(
              httpStatus(OK),
              elementText("h1")("What is your client’s VAT number?"),
              isElementVisible("#vrn")(isVisible = true)
            )
          }
        }

        "the agent has a preference of 'no' in session" should {

          "Render the Select a Client page" in {

            given.agent.isSignedUpToAgentServices

            When("I call the show Select Client VRN page")
            val res = get(path, formatPreference(Some("no")))

            res should have(
              httpStatus(OK),
              elementText("h1")("What is your client’s VAT number?"),
              isElementVisible("#vrn")(isVisible = true)
            )
          }
        }

        "the agent has no preference in session" should {

          "Render the Capture Preference page" in {

            given.agent.isSignedUpToAgentServices

            When("I call the show Select Client VRN page")
            val res = show

            res should have(
              httpStatus(SEE_OTHER),
              redirectURI(controllers.agent.routes.CapturePreferenceController.show().url)
            )
          }
        }
      }

      "the Agent is not signed up for HMRC-AS-AGENT (not authorised)" should {

        "Render the Sign up for Agent Services unauthorised view" in {

          given.agent.isNotSignedUpToAgentServices

          When("I call the show Select Client VRN page")
          val res = show

          res should have(
            httpStatus(FORBIDDEN),
            pageTitle(Messages("unauthorised.agent.title"))
          )
        }
      }
    }

    "the user is a Principle Entity and not an Agent" should {

      "render the Technical Difficulties page" in {

        given.user.isAuthenticated

        When("I call the show Select Client VRN page")
        val res = show

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR),
          pageTitle(Messages("standardError.title"))
        )
      }
    }
  }


  "Calling the .submit action" when {

    val validData = ClientVrnModel(clientVRN)
    def submit(data: ClientVrnModel): WSResponse = post(
      path,
      Map(SessionKeys.clientMandationStatus -> "Non MTDfB")
    )(toFormData(ClientVrnForm.form, data))

    "the user is an Agent" when {

      "the Agent is signed up for HMRC-AS-AGENT (authorised)" when {

        "a valid VRN is submitted" should {

          "Redirect to the Confirm Client page and add vrn to session" in {

            given.agent.isSignedUpToAgentServices

            When("I submit the Client VRN page with valid data")
            val res = submit(validData)

            res should have(
              httpStatus(SEE_OTHER),
              redirectURI(controllers.agent.routes.ConfirmClientVrnController.show().url)
            )

            SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.clientVRN) shouldBe Some(clientVRN)
            SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.clientMandationStatus) shouldBe None
          }
        }

        "an invalid VRN is submitted" should {

          "Return a Bad Request and render the view with errors" in {

            given.agent.isSignedUpToAgentServices

            When("I submit the Client VRN page with invalid data")
            val res = submit(ClientVrnModel("999999999"))

            res should have(
              httpStatus(BAD_REQUEST),

              //Error Summary
              isElementVisible("#error-summary-display")(isVisible = true),
              isElementVisible("#vrn-error-summary")(isVisible = true),
              elementText("#vrn-error-summary")("Enter a VAT number in the correct format"),
              elementWithLinkTo("#vrn-error-summary")("#vrn"),

              //Error against Input Label
              isElementVisible(".form-field--error .error-notification")(isVisible = true),
              elementText(".form-field--error .error-notification")("Enter a VAT number in the correct format")
            )
          }
        }
      }

      "the Agent is NOT signed up for HMRC-AS-AGENT (unauthorised)" when {

        "render the Agent Unauthorised page" in {

          given.agent.isNotSignedUpToAgentServices

          When("I submit the Client VRN page with valid data")
          val res = submit(validData)

          res should have(
            httpStatus(FORBIDDEN),
            pageTitle(Messages("unauthorised.agent.title"))
          )
        }
      }
    }

    "the user is a Principle Entity and not an Agent" should {

      "render the Technical Difficulties page" in {

        given.user.isAuthenticated

        When("I submit the Client VRN page with valid data")
        val res = submit(validData)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR),
          pageTitle(Messages("standardError.title"))
        )
      }
    }

    httpPostAuthenticationTests(path)(toFormData(ClientVrnForm.form, validData))
  }
}
