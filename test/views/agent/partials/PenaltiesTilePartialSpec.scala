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

import messages.partials.PenaltiesTileMessages
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.partials.PenaltiesTilePartial

class PenaltiesTilePartialSpec extends ViewBaseSpec {

  val penaltiesTilePartial: PenaltiesTilePartial = inject[PenaltiesTilePartial]

  "PenaltiesTilePartial view" when {

    "true is provided to the partial" when {
      lazy val view = penaltiesTilePartial()(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct title of ${PenaltiesTileMessages.title}" in {
        elementText("#penalties-heading") shouldBe PenaltiesTileMessages.title
      }

      s"have the correct link for the penalties-frontend homepage" in {
        element("#penalties-heading > a").attr("href") shouldBe "/vat-through-software/representative/test-only/penalties-stub"
      }

      s"have the correct card information of ${PenaltiesTileMessages.description}" in {
        elementText("#penalties-tile-content") shouldBe PenaltiesTileMessages.description
      }
    }
  }
}
