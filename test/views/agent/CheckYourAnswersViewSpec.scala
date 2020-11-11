/*
 * Copyright 2020 HM Revenue & Customs
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
    val heading         = ".heading-large"
    val heading2        = "h2"
    val backLink        = "#content > article > a"
    val email           = ".cya-answer"
    val continueButton  = ".button"
    val editLink        = ".cya-change > a"
    val editLinkText    = ".cya-change > a > span:nth-of-type(1)"
    val editLinkContext = ".cya-change > a > span:nth-of-type(2)"
  }

  "The Check Your Answers view" should {
    lazy val view = injectedView(testEmail)(request, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      document.title() shouldBe "Check your answers - Your client’s VAT details - GOV.UK"
    }

    "have the correct heading" in {
      elementText(Selectors.heading) shouldBe "Check your answers"
    }

    "have the correct subheading" in {
      elementText(Selectors.heading2) shouldBe "VAT business details"
    }

    "have the email address the user provided" in {
      elementText(Selectors.email) shouldBe testEmail
    }

    "have a link to edit email address" which {

      "has the correct text" in {
        elementText(Selectors.editLinkText) shouldBe "Change"
      }

      "has the correct link" in {
        element(Selectors.editLink).attr("href") shouldBe controllers.agent.routes.CapturePreferenceController.show().url
      }

      "has the correct hidden context text" in {
        elementText(Selectors.editLinkContext) shouldBe "Check your answers"
      }
    }

    "have a continue button" which {

      "has the correct text" in {
        elementText(Selectors.continueButton) shouldBe "Confirm and continue"
      }
    }
  }
}
