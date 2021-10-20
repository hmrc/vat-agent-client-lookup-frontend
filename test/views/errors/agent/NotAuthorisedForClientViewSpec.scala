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

package views.errors.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import assets.messages.BaseMessages
import assets.messages.{AgentUnauthorisedForClientPageMessages => Messages}
import views.html.errors.agent.NotAuthorisedForClientView

class NotAuthorisedForClientViewSpec extends ViewBaseSpec {

  val injectedView: NotAuthorisedForClientView = inject[NotAuthorisedForClientView]

  "Rendering the unauthorised for client page" should {

    object Selectors {
      val serviceName = ".hmrc-header__service-name"
      val pageHeading = "#content h1"
      val information = "#content > p:nth-child(2)"
      val agentServicesLink = "#content > p:nth-child(2) > a:nth-child(1)"
      val button = ".govuk-button"
      val backLink = ".govuk-back-link"
    }

    lazy val view = injectedView()(request, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe (Messages.title + " - Your client’s VAT details - GOV.UK")
    }

    "have the correct service name" in {
      elementText(Selectors.serviceName) shouldBe BaseMessages.agentServiceName
    }

    "have a the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe Messages.title
    }

    "have the correct paragraph on the page" in {
      elementText(Selectors.information) shouldBe Messages.information
    }

    "have a link in the paragraph" which {

      "has the correct link text" in {
        elementText(Selectors.agentServicesLink) shouldBe Messages.agentServicesAccount
      }

      "has the correct link location" in {
        element(Selectors.agentServicesLink).attr("href") shouldBe mockConfig.agentServicesUrl
      }
    }

    "have a Back link" which {

      "has the correct link text" in {
        elementText(Selectors.backLink) shouldBe BaseMessages.back
      }

      "has the correct link location" in {
        element(Selectors.backLink).attr("href") shouldBe controllers.agent.routes.SelectClientVrnController.show().url
      }
    }

    "have a Sign out button" which {

      "has the correct text" in {
        elementText(Selectors.button) shouldBe BaseMessages.signOut
      }

      "have the correct link location" in {
        element(Selectors.button).attr("href") shouldBe
          controllers.routes.SignOutController.signOut(feedbackOnSignOut = true).url
      }
    }
  }
}
