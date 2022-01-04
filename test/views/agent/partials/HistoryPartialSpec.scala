/*
 * Copyright 2022 HM Revenue & Customs
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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.partials.HistoryPartial
import assets.messages.partials.{HistoryPartialMessages => Messages}

class HistoryPartialSpec extends ViewBaseSpec {

  val historyPartial: HistoryPartial = inject[HistoryPartial]

  object Selectors {
    val heading = "#history-title"
    val link1 = "#past-payments-link"
    val link2 = "#past-returns-link"
  }

  "The payment history partial" when {

    "the user is not hybrid" should {

      lazy val view = historyPartial(hybridUser = false)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct heading" in {
        elementText(Selectors.heading) shouldBe Messages.heading
      }

      "have a first link" that {

        "has the correct text" in {
          elementText(Selectors.link1) shouldBe Messages.link1
        }

        "has the correct href" in {
          element(Selectors.link1).attr("href") shouldBe mockConfig.paymentHistoryUrl
        }
      }

      "have a second link" that {

        "has the correct text" in {
          elementText(Selectors.link2) shouldBe Messages.link2
        }

        "has the correct href" in {
          element(Selectors.link2).attr("href") shouldBe mockConfig.submittedReturnsUrl
        }
      }
    }

    "the user is hybrid" should {

      lazy val view = historyPartial(hybridUser = true)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not have a past payments link" in {
        elementExtinct(Selectors.link1)
      }
    }
  }


}
