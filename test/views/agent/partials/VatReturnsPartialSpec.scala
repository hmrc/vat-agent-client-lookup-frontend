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

import assets.messages.partials.{VatReturnsPartialMessages => Messages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec

class VatReturnsPartialSpec extends ViewBaseSpec {

  "VatReturnsPartial" should {

    "passed a mandation status of 'Non MTDfB'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.vatReturnsPartial("Non MTDfB")(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct card heading" in {
        elementText(".heading-medium") shouldBe Messages.heading
      }

      "display the body of text for the card" in {
        elementText("#card-info") shouldBe Messages.paragraphOneNonMandated
      }

      "display the correct text in link 1" in {
        elementText("#card-link-submit-returns") shouldBe Messages.submitVatReturns
      }

      "the 1st link has the correct url" in {
        element("#card-link-submit-returns").attr("href") shouldBe "guidance/submit-vat-returns"
      }

      "display the correct text in link 2" in {
        elementText("#card-link-view-returns") shouldBe Messages.submittedReturns
      }

      "the 2nd link has the correct url" in {
        element("#card-link-view-returns").attr("href") shouldBe "/submitted-returns"
      }
    }

    "passed a mandation status of 'MTDfB Mandated'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.vatReturnsPartial("MTDfB Mandated")(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct card heading" in {
        elementText(".heading-medium") shouldBe Messages.heading
      }

      "display the correct body of text for the card" in {
        elementText("#card-info") shouldBe Messages.paragraphOneMandated
      }

      "display the correct line 2" in {
        elementText("#card-link-view-returns") shouldBe Messages.submittedReturns
      }

      "display the correct line 2 with the correct link" in {
        element("#card-link-view-returns").attr("href")
      }

    }
  }
}
