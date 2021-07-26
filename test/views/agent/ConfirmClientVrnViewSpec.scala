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

package views.agent

import assets.{BaseTestConstants, CustomerDetailsTestConstants}
import assets.messages.{BaseMessages, ConfirmClientVrnPageMessages => viewMessages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.ConfirmClientVrnView

class ConfirmClientVrnViewSpec extends ViewBaseSpec {

  val injectedView: ConfirmClientVrnView = inject[ConfirmClientVrnView]

  object Selectors {
    val title = "h1"
    val clientNameTitle = ".govuk-summary-list__row:nth-child(1) > dt"
    val clientName = ".govuk-summary-list__row:nth-child(1) > dd"
    val vrnTitle = ".govuk-summary-list__row:nth-child(2) > dt"
    val vrn = ".govuk-summary-list__row:nth-child(2) > dd"
    val button = ".govuk-button"
    val changeClientLink = "#change-client"
  }

  "The Confirm Change Client VRN page" when {

    "given an individual with no trading name" should {

      lazy val view = injectedView(
        BaseTestConstants.vrn,
        CustomerDetailsTestConstants.customerDetailsIndividual
      )(request, messages, mockConfig)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct document title of '${viewMessages.title}'" in {
        document.title shouldBe (viewMessages.title + " - Your client’s VAT details - GOV.UK")
      }

      s"have the correct page heading of '${viewMessages.heading}'" in {
        elementText(Selectors.title) shouldBe viewMessages.heading
      }

      "have the correct heading and text for the business name section" in {
        elementText(Selectors.clientNameTitle) shouldBe viewMessages.name
        elementText(Selectors.clientName) shouldBe CustomerDetailsTestConstants.userName
      }

      "have the correct heading and text for the VAT number section" in {
        elementText(Selectors.vrnTitle) shouldBe viewMessages.vrn
        elementText(Selectors.vrn) shouldBe BaseTestConstants.vrn
      }

      "have a confirm button" which {

        s"has the text '${BaseMessages.confirm}'" in {
          elementText(Selectors.button) shouldBe BaseMessages.confirmAndContinue
        }

        "has a link to the what to do URL" in {
          element(Selectors.button).attr("href") shouldBe controllers.agent.routes.ConfirmClientVrnController.redirect().url
        }
      }

      "have a change client link" which {

        s"has the text '${viewMessages.edit}'" in {
          elementText(Selectors.changeClientLink) shouldBe viewMessages.edit
        }

        "has the correct URL" in {
          element(Selectors.changeClientLink).attr("href") shouldBe controllers.agent.routes.ConfirmClientVrnController.changeClient().url
        }
      }
    }

    "given an individual with a trading name" should {

      lazy val view = injectedView(
        BaseTestConstants.vrn,
        CustomerDetailsTestConstants.customerDetailsAllInfo
      )(request, messages, mockConfig)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct document title of '${viewMessages.title}'" in {
        document.title shouldBe (viewMessages.title + " - Your client’s VAT details - GOV.UK")
      }

      s"have the correct page heading of '${viewMessages.heading}'" in {
        elementText(Selectors.title) shouldBe viewMessages.heading
      }

      "have the correct heading and text for the client name section" in {
        elementText(Selectors.clientNameTitle) shouldBe viewMessages.name
        elementText(Selectors.clientName) shouldBe CustomerDetailsTestConstants.tradingName
      }
    }
  }
}
