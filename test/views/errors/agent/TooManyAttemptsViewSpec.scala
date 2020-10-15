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

package views.errors.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.errors.agent.TooManyAttemptsView

class TooManyAttemptsViewSpec extends ViewBaseSpec {

  val injectedView: TooManyAttemptsView = inject[TooManyAttemptsView]
  lazy val view: Html = injectedView()(messages, mockConfig)

  lazy implicit val document: Document = Jsoup.parse(view.body)

  "The Too many attempts view" should {

    s"have the correct document title" in {
      document.title shouldBe "You need to start again - Your client’s VAT details - GOV.UK"
    }

    "have the correct heading" in {
      document.getElementsByClass("heading-large").text() shouldBe "You need to start again"
    }

    "have the correct text for the first paragraph" in {
      elementText("#content article p:nth-of-type(1)") shouldBe "This is because you have entered the wrong code too many times."
    }

    "have the correct text for the second paragraph" in {
      elementText("#content article p:nth-of-type(2)") shouldBe "Return to your client’s VAT account " +
        "to start the process again."
    }

    "have a link" which {

      "has the correct link text" in {
        elementText("#content > article > p:nth-of-type(2) > a") shouldBe "Return to your client’s VAT account"
      }

      "has the correct href" in {
        element("#content > article > p:nth-of-type(2) > a").attr("href") shouldBe
          controllers.agent.routes.AgentHubController.show().url
      }
    }
  }
}
