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

package pages.agent

import helpers.IntegrationTestConstants._
import pages.BasePageISpec
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import stubs.EmailVerificationStub

class ConfirmEmailPageSpec extends BasePageISpec {

  val path = "/email-confirmation"
  val isEmailVerifiedPath = "/is-email-verified"

  "Calling the .show action" when {

    "the user is an Agent" when {

      "the Agent is signed up for HMRC-AS-AGENT" when {

        "there is no notification email in session" should {

          "Render the Confirm Email view" in {

            def show(): WSResponse = get(path, formatNotificationsEmail(None) ++ formatReturnUrl)

            given.agent.isSignedUpToAgentServices

            When("I call the Confirm email page with no notification email in session")

            val res = show()

            res should have(
              httpStatus(SEE_OTHER),
              redirectURI(controllers.agent.routes.CapturePreferenceController.show().url)
            )
          }
        }

        "there is a notification email in session" should {

          "Render the Confirm Email view" in {

            def show(): WSResponse = get(path, formatNotificationsEmail(Some(notificationsEmail)) ++ formatReturnUrl)

            given.agent.isSignedUpToAgentServices

            When("I call the Confirm email page with a notification email in session")
            val res = show()

            res should have(
              httpStatus(OK),
              elementText("h1")("Confirm the new email address")
            )
          }
        }
      }
    }
  }

  "Calling the .isEmailVerified action" when {

    "the user is an Agent" when {

      "the Agent is signed up for HMRC-AS-AGENT" when {

        "there is a verified notification email in session" should {

          "Render the Select client VRN view" in {

            val path = "/is-email-verified"

            def isEmailVerified: WSResponse = get(path, formatNotificationsEmail(Some(notificationsEmail)) ++
              formatReturnUrl)

            given.agent.isSignedUpToAgentServices

            And("I stub an email verified successful response from EmailVerificationService")
            EmailVerificationStub.stubEmailVerified

            When("I call the isEmailVerified controller action with a verified notification email in session")

            val res = isEmailVerified

            res should have(
              httpStatus(SEE_OTHER),
              redirectURI(controllers.agent.routes.SelectClientVrnController.show().url)
            )
          }
        }

        "there is a non-verified notification email in session" should {

          "Render the Verify Email view" in {

            def isEmailVerified: WSResponse = get(isEmailVerifiedPath, formatNotificationsEmail(Some(notificationsEmail)) ++
              formatReturnUrl)

            given.agent.isSignedUpToAgentServices

            And("I stub an email not verified response from EmailVerificationService")
            EmailVerificationStub.stubEmailNotVerified

            When("I call the isEmailVerified controller action with a non-verified notification email in session")

            val res = isEmailVerified

            res should have(
              httpStatus(SEE_OTHER),
              redirectURI(controllers.agent.routes.VerifyEmailController.sendVerification().url)
            )
          }
        }

        "there is a notification email in session but there was an error when checking the verification status" should {

          "Render the Error view" in {

            def isEmailVerified: WSResponse = get(isEmailVerifiedPath, formatNotificationsEmail(Some(notificationsEmail)) ++
              formatReturnUrl)

            given.agent.isSignedUpToAgentServices

            And("I stub an error response from EmailVerificationService")
            EmailVerificationStub.stubEmailVerifiedError

            When("I call the isEmailVerified controller action with a notification email in session")

            val res = isEmailVerified

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR),
              elementText("h1")("Sorry, there is a problem with the service")
            )
          }
        }

        "there is no notification email in session" should {

          "Render the Error view" in {

            def isEmailVerified: WSResponse = get(isEmailVerifiedPath, formatNotificationsEmail(None) ++
              formatReturnUrl)

            given.agent.isSignedUpToAgentServices

            When("I call the isEmailVerified controller action with no notification email in session")

            val res = isEmailVerified

            res should have(
              httpStatus(SEE_OTHER),
              redirectURI(controllers.agent.routes.CapturePreferenceController.show().url)
            )
          }
        }
      }
    }
  }

  httpGetAuthenticationTests(path, Some(clientVRN))
}
