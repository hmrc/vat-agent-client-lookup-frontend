/*
 * Copyright 2021 HM Revenue & Customs
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
import assets.messages.partials.{SignUpPartialMessages => Messages}
import common.MandationStatus
import views.html.agent.partials.SignUpPartial

class SignUpPartialSpec extends ViewBaseSpec {

  val signUpPartial: SignUpPartial = inject[SignUpPartial]

  object Selectors {
    val link = "#sign-up-link"
    val content = "#sign-up-body"
  }

  "sign up partial" when {

    "passed a mandation status of 'MTDfB Exempt'" should {

      lazy val view = signUpPartial(MandationStatus.MTDfBExempt,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct link text" in {
        elementText(Selectors.link) shouldBe Messages.signUpLinkText
      }

      s"display the correct link of ${mockConfig.signUpServiceUrl(user.vrn)}" in {
        element(Selectors.link).attr("href") shouldBe mockConfig.signUpServiceUrl(user.vrn)
      }

      "display the correct body of text" in {
        elementText(Selectors.content) shouldBe Messages.signUpBody
      }
    }

    "passed a mandation status of 'Non-MTD'" should {

      lazy val view = signUpPartial(MandationStatus.nonMTDfB,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct link text" in {
        elementText(Selectors.link) shouldBe Messages.signUpLinkText
      }

      s"display the correct link of ${mockConfig.signUpServiceUrl(user.vrn)}" in {
        element(Selectors.link).attr("href") shouldBe mockConfig.signUpServiceUrl(user.vrn)
      }

      "display the correct body of text" in {
        elementText(Selectors.content) shouldBe Messages.signUpBody
      }
    }

    "passed a mandation status of 'Non Digital'" should {

      lazy val view = signUpPartial(MandationStatus.nonDigital,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct link text" in {
        elementText(Selectors.link) shouldBe Messages.signUpLinkText
      }

      s"display the correct link of ${mockConfig.signUpServiceUrl(user.vrn)}" in {
        element(Selectors.link).attr("href") shouldBe mockConfig.signUpServiceUrl(user.vrn)
      }

      "display the correct body of text" in {
        elementText(Selectors.content) shouldBe Messages.signUpBody
      }

    }

    "passed a mandation status of 'MTD Mandated'" should {

      lazy val view = signUpPartial(MandationStatus.manMTDfB,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display link text" in {
        elementExtinct(Selectors.link)
      }

      "not display body of text" in {
        elementExtinct(Selectors.content)
      }
    }

    "passed a mandation status of 'MTDfB Voluntary'" should {

      lazy val view = signUpPartial(MandationStatus.volMTDfB,user.vrn)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display link text" in {
        elementExtinct(Selectors.link)
      }

      "not display body of text" in {
        elementExtinct(Selectors.content)
      }

    }
  }
}
