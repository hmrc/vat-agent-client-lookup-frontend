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

class ConfirmEmailViewSpec extends ViewBaseSpec {

  val testEmail: String = "test@email.com"

  object Selectors {
    val heading         = ".heading-large"
    val heading2        = ".lede"
    val backLink        = "#content > article > a"
    val continueButton  = ".button"
    val editLink        = "#content > article > p > a"
    val editLinkText    = "#content > article > p > a > span:nth-of-type(1)"
    val editLinkContext = "#content > article > p > a > span:nth-of-type(2)"
  }

  "The Confirm Email view" should {
    lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
    lazy val view = views.html.agent.confirmEmail(testEmail)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      document.title() shouldBe "Confirm the email address - Client’s VAT details - GOV.UK"
    }

    "have the correct heading" in {
      elementText(Selectors.heading) shouldBe "Confirm the email address"
    }

    "have a back link" which {

      "should have the correct text" in {
        elementText(Selectors.backLink) shouldBe "Back"
      }

      "should have the correct back link" in {
        element(Selectors.backLink).attr("href") shouldBe controllers.agent.routes.CapturePreferenceController.show().url
      }
    }

    "have the email address the user provided" in {
      elementText(Selectors.heading2) shouldBe testEmail
    }

    "have a link to edit email address" which {

      "has the correct text" in {
        elementText(Selectors.editLinkText) shouldBe "Change"
      }

      "has the correct link" in {
        element(Selectors.editLink).attr("href") shouldBe controllers.agent.routes.CapturePreferenceController.show().url
      }

      "has the correct hidden context text" in {
        elementText(Selectors.editLinkContext) shouldBe "Change your email address"
      }

      "has the correct GA tag" in {
        element(Selectors.editLink).attr("data-journey-click") shouldBe "notification-pref:change:confirm-email"
      }
    }

    "have a continue button" which {

      "has the correct text" in {
        elementText(Selectors.continueButton) shouldBe "Confirm and continue"
      }

      "has the correct GA tag" in {
        element(Selectors.continueButton).attr("data-journey-click") shouldBe "notification-pref:confirm:confirm-email"
      }
    }
  }
}
