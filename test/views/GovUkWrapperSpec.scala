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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.GovukWrapper

class GovUkWrapperSpec extends ViewBaseSpec {

  val injectedView: GovukWrapper = inject[GovukWrapper]

  val navTitleSelector = ".header__menu__proposition-name"
  val accessibilityLinkSelector = "#footer > div > div > div.footer-meta-inner > ul > li:nth-child(2) > a"

  "The GOV UK Wrapper" when {

    "the user is an agent" should {

      lazy val view = injectedView(mockConfig,"Test")(request, messages)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not be shown a logo" in {
        document.select(".organisation-logo") shouldBe empty
      }

      "have a nav title of 'Your client’s VAT details'" in {
        elementText(navTitleSelector) shouldBe "Your client’s VAT details"
      }

      "have the correct Accessibility link" in {
        element(accessibilityLinkSelector).attr("href") shouldBe "/accessibility-statement"
      }
    }
  }
}

