/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import stubs.{PenaltiesStub, VatSubscriptionStub}


class AgentHubPageSpec extends BasePageISpec {

  val path = "/client-vat-account"

  val penaltiesJson = Json.parse(
    """
      |{
      |  "noOfPoints": 3,
      |  "noOfEstimatedPenalties": 2,
      |  "noOfCrystalisedPenalties": 1,
      |  "estimatedPenaltyAmount": 123.45,
      |  "crystalisedPenaltyAmountDue": 54.32,
      |  "hasAnyPenaltyData": true
      |}
      |""".stripMargin)

  def show(sessionVrn: Option[String] = None): WSResponse = get(path, formatSessionVrn(sessionVrn) ++ formatReturnUrl)

  "Calling the .show() action" when {

    "the user is an Agent" when {

      "the Agent is signed up for HMRC-AS-AGENT (authorised)" should {

        "Render the Agent Hub page with View Return Deadlines" in {

          given.agent.isSignedUpToAgentServices

          And("I stub a successful response from vat subscription")
          VatSubscriptionStub.getClientDetailsSuccess(clientVRN)(individualMandatedJson)

          When("I call the Agent Hub page with the clients VRN in the session")
          val res = show(Some(clientVRN))
          print(res)

          res should have(
            httpStatus(OK),
            pageTitle(Messages("Your client’s VAT details") + " - Your client’s VAT details - GOV.UK"),
            elementText("#next-return-link")("View return deadlines")
          )

        }

        "Render the Agent Hub page with the Submit Vat Return" in {

          given.agent.isSignedUpToAgentServices

          And("I stub a successful response from vat subscription")
          VatSubscriptionStub.getClientDetailsSuccess(clientVRN)(individualNonMTDfBJson)

          When("I call the Agent Hub page with the clients VRN in the session")
          val res = show(Some(clientVRN))
          print(res)

          res should have(
            httpStatus(OK),
            pageTitle(Messages("Your client’s VAT details") + " - Your client’s VAT details - GOV.UK"),
            elementText("#next-return-link")("Submit VAT Return")
          )

        }

        "Render the Agent Hub page with the penalties banner" in {

          given.agent.isSignedUpToAgentServices

          And("I stub a successful response from vat subscription and penalties")
          VatSubscriptionStub.getClientDetailsSuccess(clientVRN)(individualMandatedJson)
          PenaltiesStub.stubPenaltiesSummary(Status.OK, penaltiesJson, clientVRN)

          When("I call the Agent Hub page with the clients VRN in the session")
          val res = show(Some(clientVRN))
          print(res)

          res should have(
            httpStatus(OK),
            pageTitle(Messages("Your client’s VAT details") + " - Your client’s VAT details - GOV.UK"),
            elementText("#govuk-notification-banner-title-penalties-banner")("Late submission and late payment penalties")
          )

        }

        "an error response is received for the Customer Details" should {

          "Render the Internal Server Error view" in {

            given.agent.isSignedUpToAgentServices

            And("I stub an error response from vat subscription")
            VatSubscriptionStub.getClientDetailsError(clientVRN)

            When("I call the Agent Hub Page with the clients VRN in the session")
            val res = show(Some(clientVRN))

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR),
              pageTitle(Messages("standardError.title") + " - Your client’s VAT details - GOV.UK")
            )

          }
        }
      }
    }
  }
}


