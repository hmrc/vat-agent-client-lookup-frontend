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

package views.agent.partials

import assets.messages.partials.{NextReturnPartialMessages => Messages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.partials.NextReturnPartial

class NextReturnPartialSpec extends ViewBaseSpec {

  val nextReturnPartial: NextReturnPartial = inject[NextReturnPartial]

  object Selectors {
    val heading = "#next-return-heading"
    val link = "#next-return-link"
  }

  "NextReturnPartial" when {

    "the user is opted in" should {

      lazy val view = nextReturnPartial(optedIn = true)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct card heading" in {
        elementText(Selectors.heading) shouldBe Messages.heading
      }

      "display the correct text in the link" in {
        elementText(Selectors.link) shouldBe Messages.viewReturnDeadlines
      }

      "have the correct url for the link" in {
        element(Selectors.link).getAllElements.attr("href") shouldBe mockConfig.returnDeadlinesUrl
      }
    }

    "the user is opted out" should {

      lazy val view = nextReturnPartial(optedIn = false)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct text in the link" in {
        elementText(Selectors.link) shouldBe Messages.submitVatReturn
      }
    }
  }
}
