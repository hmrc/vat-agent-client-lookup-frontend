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

package views.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec
import assets.BaseTestConstants.vrn
import assets.CustomerDetailsTestConstants.customerDetailsAllInfo
import assets.messages.{AgentHubMessages => Messages}

class AgentHubViewSpec extends ViewBaseSpec {

  "AgentHubPage" when {

    "the user is a valid agent" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.agentHub(customerDetailsAllInfo, vrn)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe Messages.title
      }

      "display the correct heading" in {
        elementText("h1") shouldBe Messages.heading
      }

      "display the correct client details" in {
        elementText(".form-hint") should include(Messages.vatNo(vrn))
        elementText(".form-hint") should include(customerDetailsAllInfo.clientName)
        elementText(".form-hint > a") shouldBe Messages.changeClient
      }

      "has the correct URL for changing client" in {
        element(".form-hint > a").attr("href") shouldBe controllers.agent.routes.ConfirmClientVrnController.changeClient().url
      }

      "have a breadcrumb link to agent services" in {
        elementText("#breadcrumb-asa") shouldBe Messages.agentServicesAccount
        element("#breadcrumb-asa").attr("href") shouldBe mockConfig.agentServicesUrl
      }
    }
  }

}
