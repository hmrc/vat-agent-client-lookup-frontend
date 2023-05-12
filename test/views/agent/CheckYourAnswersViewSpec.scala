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

package views.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewBaseSpec {

  val injectedView: CheckYourAnswersView = inject[CheckYourAnswersView]

  val testEmail: String = "test@email.com"

  object Selectors {
    val heading         = ".govuk-heading-l"
    val heading2        = ".govuk-heading-m"
    val email           = ".govuk-summary-list__value"
    val continueButton  = ".govuk-button"
    val editLink        = ".govuk-summary-list__actions > a"
    val editLinkMessage = ".govuk-summary-list__actions > a > span:nth-child(1)"
    val editLinkHiddenMessage = ".govuk-summary-list__actions > a > span.govuk-visually-hidden"
  }

  "The Check Your Answers view" should {
    lazy val view = injectedView(testEmail)(request, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      document.title() shouldBe "Check your answer - Your client’s VAT details - GOV.UK"
    }

    "have the correct heading" in {
      elementText(Selectors.heading) shouldBe "Check your answer"
    }

    "have the correct subheading" in {
      elementText(Selectors.heading2) shouldBe "VAT business details"
    }

    "have the email address the user provided" in {
      elementText(Selectors.email) shouldBe testEmail
    }

    "have a link to edit email address" which {

      "has the correct text" in {
        elementText(Selectors.editLinkMessage) shouldBe "Change"
      }

      "has the correct link" in {
        element(Selectors.editLink).attr("href") shouldBe controllers.agent.routes.CapturePreferenceController.show().url
      }

      "has the correct hidden context text" in {
        elementText(Selectors.editLinkHiddenMessage) shouldBe "Change the email address"
      }
    }

    "have a form with the correct action" in {
      element("form").attr("action") shouldBe "/vat-through-software/representative/is-email-verified"
    }

    "have a continue button" which {

      "has the correct text" in {
        elementText(Selectors.continueButton) shouldBe "Confirm and continue"
      }

      "has the prevent double click attribute" in {
        element(Selectors.continueButton).hasAttr("data-prevent-double-click") shouldBe true
      }
    }
  }
}
