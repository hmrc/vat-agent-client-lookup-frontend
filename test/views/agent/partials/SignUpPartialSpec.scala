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

package views.agent.partials

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec
import assets.messages.partials.{SignUpPartialMessages => Messages}
import common.MandationStatus

class SignUpPartialSpec extends ViewBaseSpec {

  "sign up partial" when {

    "passed a mandation status of 'MTDfB Exempt'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.signUpPartial(MandationStatus.MTDfBExempt,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct link text" in {
        elementText("a") shouldBe Messages.signUpLinkText
      }

      s"display the correct link of ${mockConfig.signUpServiceUrl(user.vrn)}" in {
        element("a").attr("href") shouldBe mockConfig.signUpServiceUrl(user.vrn)
      }

      "display the correct body of text" in {
        elementText("p") shouldBe Messages.signUpBody
      }
    }

    "passed a mandation status of 'Non-MTD'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.signUpPartial(MandationStatus.nonMTDfB,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct link text" in {
        elementText("a") shouldBe Messages.signUpLinkText
      }

      s"display the correct link of ${mockConfig.signUpServiceUrl(user.vrn)}" in {
        element("a").attr("href") shouldBe mockConfig.signUpServiceUrl(user.vrn)
      }

      "display the correct body of text" in {
        elementText("p") shouldBe Messages.signUpBody
      }
    }

    "passed a mandation status of 'Non Digital'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.signUpPartial(MandationStatus.nonDigital,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct link text" in {
        elementText("a") shouldBe Messages.signUpLinkText
      }

      s"display the correct link of ${mockConfig.signUpServiceUrl(user.vrn)}" in {
        element("a").attr("href") shouldBe mockConfig.signUpServiceUrl(user.vrn)
      }

      "display the correct body of text" in {
        elementText("p") shouldBe Messages.signUpBody
      }

    }

    "passed a mandation status of 'MTD Mandated'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.signUpPartial(MandationStatus.manMTDfB,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display link text" in {
        elementExtinct("a")
      }

      "not display body of text" in {
        elementExtinct("p")
      }
    }

    "passed a mandation status of 'MTDfB Voluntary'" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = views.html.agent.partials.signUpPartial(MandationStatus.volMTDfB,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display link text" in {
        elementExtinct("a")
      }

      "not display body of text" in {
        elementExtinct("p")
      }

    }
  }
}
