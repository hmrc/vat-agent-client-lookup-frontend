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

package views.agent.partials

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec
import assets.messages.partials.OptOutForMTDVATMessages
import common.MandationStatus

class OptOutForMTDVATPartialSpec extends ViewBaseSpec {

  "OptOutForMTDVATPartial view" when {

    "with a mandated status" should {
      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.optOutForMTDVATPartial(MandationStatus.mandated)(messages,mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct title of ${OptOutForMTDVATMessages.title}" in {
        elementText(".heading-medium") shouldBe OptOutForMTDVATMessages.title
      }

      s"have the correct link of ${mockConfig.optOutMtdVatUrl}" in {
        element("a").attr("href") shouldBe mockConfig.optOutMtdVatUrl
      }

      s"have the correct card information of ${OptOutForMTDVATMessages.description}" in {
        elementText("p") shouldBe OptOutForMTDVATMessages.description
      }

    }

    "with a mandation status of nonMTDfB" should {

      lazy val view = views.html.agent.partials.optOutForMTDVATPartial(MandationStatus.nonMTDfB)(messages ,mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not be displayed" in {

        document.select(".card") shouldBe empty

      }
    }
  }
}
