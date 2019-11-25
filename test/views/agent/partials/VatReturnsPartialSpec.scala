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

import java.time.LocalDate

import assets.messages.partials.{VatReturnsPartialMessages => Messages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec

class VatReturnsPartialSpec extends ViewBaseSpec {

  "VatReturnsPartial" when {

    val date: LocalDate = LocalDate.parse("2018-05-01")

    "passed a mandation status of 'Non MTDfB'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.vatReturnsPartial(, date)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct card heading" in {
        elementText(".heading-medium") shouldBe Messages.heading
      }

      "display the body of text for the card" in {
        elementText("p:nth-of-type(1)") shouldBe Messages.paragraphOneNonMandated
      }

      "display the correct text in link 1" in {
        elementText("li:nth-child(1)") shouldBe Messages.submitVatReturn
      }

      "have the correct url for the 1st link" in {
        element("li:nth-child(1)").getAllElements().attr("href") shouldBe "/vat-through-software/vat-returns/return-deadlines"
      }

      "display the correct text in link 2" in {
        elementText("li:nth-child(2)") shouldBe Messages.submittedReturns
      }

      "have the correct url for the 2nd link" in {
        element("li:nth-child(2)").getAllElements().attr("href") shouldBe "/submitted-returns/2018"
      }
    }

    "passed a mandation status of 'MTDfB Mandated'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.vatReturnsPartial("MTDfB Mandated", date)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct card heading" in {
        elementText(".heading-medium") shouldBe Messages.heading
      }

      "display the correct body of text for the card" in {
        elementText("p:nth-of-type(1)") shouldBe Messages.paragraphOneMandated
      }

      "display the correct text in link 1" in {
        elementText("li") shouldBe Messages.submittedReturns
      }

      "have the correct url for the 1st link" in {
        element("li").getAllElements().attr("href") shouldBe "/submitted-returns/2018"
      }
    }
  }
}
