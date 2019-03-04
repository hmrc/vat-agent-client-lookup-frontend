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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec

class NotSignedUpViewSpec extends ViewBaseSpec {

  "Rendering the Not Signed Up error page" should {

    lazy val view = views.html.errors.notSignedUp()(request, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      document.title shouldBe "Your client’s VAT details"
    }

    "have the correct heading" in {
      elementText("h1") shouldBe "The business has not signed up to Making Tax Digital for VAT"
    }

    "have the correct main paragraph" in {
      elementText("#content p:nth-of-type(1)") shouldBe
        "You cannot use this service for your client’s business as the business has not signed up for Making Tax Digital for VAT."
    }

    "have the correct list header text" in {
      elementText("h2") shouldBe "What happens next"
    }

    "have the guidance list which" should {

      "have contain the first list item which" should {

        "have the correct text" in {
          elementText("#content li:nth-of-type(1)") shouldBe
            "sign up for Making Tax Digital for VAT (opens in a new tab) to make changes to your client’s business details"
        }

        "have the correct href" in {
          element("#content li:nth-of-type(1) > a").attr("href") shouldBe "guidance/agent-sign-up"
        }
      }

      "have contain the second list item which" should {

        "have the correct text" in {
          elementText("li:nth-of-type(2)") shouldBe
            "submit VAT Returns and manage your client’s business details (opens in a new tab) without signing up to Making Tax Digital for VAT"
        }

        "have the correct href" in {
          element("li:nth-of-type(2) > a").attr("href") shouldBe "guidance/submit-vat-returns"
        }
      }
    }

    "have the correct link for making changes to a different client which" should {

      "have the correct text" in {
        elementText("#content article > a") shouldBe "Make changes for a different client"
      }

      "have the correct href" in {
        element("#content article > a").attr("href") shouldBe "/vat-through-software/representative/client-vat-number"
      }
    }
  }
}
