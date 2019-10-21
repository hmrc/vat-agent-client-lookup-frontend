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

package views.agent

import assets.messages.{WhatToDoMessages => Messages}
import forms.WhatToDoForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec

class WhatToDoViewSpec extends ViewBaseSpec {

  "WhatToDo view" when {

    "passed a mandation status of 'Non MTDfB'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.whatToDo(WhatToDoForm.whatToDoForm, "l'biz", "Non MTDfB")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct document title" in {
        document.title shouldBe ("What would you like to do for l'biz? - Your client’s VAT details - GOV.UK")
      }

      "display the correct heading" in {
        elementText("#page-heading") shouldBe Messages.title("l'biz")
      }
      "display the correct radio options" in {
        elementText(".multiple-choice:nth-of-type(1) label") shouldBe Messages.submitReturn
        elementText(".multiple-choice:nth-of-type(2) label") shouldBe Messages.viewReturn
        elementText(".multiple-choice:nth-of-type(3) label") shouldBe Messages.changeDetails
        elementText(".multiple-choice:nth-of-type(4) label") shouldBe Messages.viewCertificate
      }
      "display the continue button" in {
        elementText("#continue") shouldBe Messages.continue
      }

    }

    "passed a mandation status of 'MTDfB Mandated'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.whatToDo(WhatToDoForm.whatToDoForm, "l'biz", "MTDfB Mandated")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct document title" in {
        document.title shouldBe ("What would you like to do for l'biz? - Your client’s VAT details - GOV.UK")
      }

      "display the correct heading" in {
        elementText("#page-heading") shouldBe Messages.title("l'biz")
      }
      "display the correct radio options" in {
        elementText(".multiple-choice:nth-of-type(1) label") shouldBe Messages.viewReturn
        elementText(".multiple-choice:nth-of-type(2) label") shouldBe Messages.changeDetails
        elementText(".multiple-choice:nth-of-type(3) label") shouldBe Messages.viewCertificate
      }
      "not display the submit or view return options" in {
        document.getElementById("submit-return") shouldBe null
      }
      "display the continue button" in {
        elementText("#continue") shouldBe Messages.continue
      }

    }

    "passed a mandation status that is not valid" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.whatToDo(WhatToDoForm.whatToDoForm, "l'biz", "Some other value")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct document title" in {
        document.title shouldBe ("What would you like to do for l'biz? - Your client’s VAT details - GOV.UK")
      }

      "display the correct heading" in {
        elementText("#page-heading") shouldBe Messages.title("l'biz")
      }
      "display the correct radio options" in {
        elementText(".multiple-choice:nth-of-type(1) label") shouldBe Messages.changeDetails
        elementText(".multiple-choice:nth-of-type(2) label") shouldBe Messages.viewCertificate
      }
      "not display the submit or view return options" in {
        document.getElementById("submit-return") shouldBe null
        document.getElementById("view-return") shouldBe null
      }
      "display the continue button" in {
        elementText("#continue") shouldBe Messages.continue
      }

    }
  }

}
