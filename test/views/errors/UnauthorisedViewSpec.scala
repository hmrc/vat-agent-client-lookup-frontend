/*
 * Copyright 2018 HM Revenue & Customs
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

class UnauthorisedViewSpec extends ViewBaseSpec {

  "Rendering the unauthorised page" should {

    object Selectors {
      val heading = "h1"
      val paragraph = "#content p"
      val setupAccountLink = "#content p > a"
      val signOutLink = "#content .button"
    }

    lazy val view = views.html.errors.unauthorised()
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe "You can’t use this service yet"
    }

    "have a the correct page heading" in {
      elementText(Selectors.heading) shouldBe "You can’t use this service yet"
    }

    "have the correct lede paragraph" in {
      elementText(Selectors.paragraph) shouldBe "To use this service, you need to set up an agent services account."
    }

    "have the correct link text to set up an agent services account" in {
      elementText(Selectors.setupAccountLink) shouldBe "set up an agent services account"
    }

    "have the correct link destination to set up an agent services account" in {
      element(Selectors.setupAccountLink).attr("href") shouldBe "/setup-agent-services-account"
    }

    "have the correct sign out link text" in {
      elementText(Selectors.signOutLink) shouldBe "Sign out"
    }

    "have the correct sign out link destination" in {
      element(Selectors.signOutLink).attr("href") shouldBe "/vat-through-software/sign-out"
    }
  }
}
