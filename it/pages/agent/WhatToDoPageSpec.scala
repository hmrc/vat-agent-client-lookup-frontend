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

import helpers.IntegrationTestConstants.{clientVRN, individualMandatedJson, individualNonMTDfBJson}
import pages.BasePageISpec
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import stubs.VatSubscriptionStub
import assets.WhatToDoConstants._

class WhatToDoPageSpec extends BasePageISpec {

  val path = "/agent-action"

  "Calling the .show() action" when {

    def show: WSResponse = get(path, formatSessionVrn(Some(clientVRN)))

    "the user is an Agent" when {

      "the Agent is signed up for HMRC-AS-AGENT (authorised)" when {

        "the user has a mandation status of 'Non MTDfB'" should {

          "Render the WhatToDo page" in {

            given.agent.isSignedUpToAgentServices
            VatSubscriptionStub.getClientDetailsSuccess(clientVRN)(individualNonMTDfBJson)

            When("I call the show WhatToDo page")
            val res = show

            res should have(
              httpStatus(OK),
              elementText("h1")(heading("PepsiCo")),
              elementText(".multiple-choice:nth-of-type(1) label")(submitReturn),
              elementText(".multiple-choice:nth-of-type(2) label")(viewReturn),
              elementText(".multiple-choice:nth-of-type(3) label")(changeDetails),
              elementText(".multiple-choice:nth-of-type(4) label")(viewCertificate)
            )
          }
        }

        "the user has a mandation status other than 'Non MTDfB'" should {

          "Render the WhatToDo page" in {

            given.agent.isSignedUpToAgentServices
            VatSubscriptionStub.getClientDetailsSuccess(clientVRN)(individualMandatedJson)

            When("I call the show WhatToDo page")
            val res = show

            res should have(
              httpStatus(OK),
              elementText("h1")(heading("PepsiCo")),
              elementText(".multiple-choice:nth-of-type(1) label")(viewReturn),
              elementText(".multiple-choice:nth-of-type(2) label")(changeDetails),
              elementText(".multiple-choice:nth-of-type(3) label")(viewCertificate)
            )
          }
        }
      }

      "the Agent is not signed up for HMRC-AS-AGENT (not authorised)" should {

        "redirect the user to the 'agent does not have delegated authority page'" in {

          given.agent.isNotSignedUpToAgentServices

          When("I call the show WhatToDo page")
          val res = show

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(controllers.agent.routes.AgentUnauthorisedForClientController.show().url)
          )
        }
      }
    }
  }
}
