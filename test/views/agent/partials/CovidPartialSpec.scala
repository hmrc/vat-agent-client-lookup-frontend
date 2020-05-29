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
import views.ViewBaseSpec
import assets.messages.partials._
import views.html.agent.partials.Covid

class CovidPartialSpec extends ViewBaseSpec {

  val covid: Covid = injector.instanceOf[Covid]

  "The covid partial" should {

    lazy val view = covid()(messages)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"have the correct card heading of ${CovidPartialMessages.heading}" in {
      elementText(".heading-medium") shouldBe CovidPartialMessages.heading
    }

    s"display the correct line 1 of ${CovidPartialMessages.line1}" in {
      elementText("p:nth-of-type(1)") shouldBe CovidPartialMessages.line1
    }

    "display the bullet points" in {
      elementText("ul > li:nth-of-type(1)") shouldBe CovidPartialMessages.bullet1
      elementText("ul > li:nth-of-type(2)") shouldBe CovidPartialMessages.bullet2
    }

    s"display the correct line 2 of ${CovidPartialMessages.line2}" in {
      elementText("p:nth-of-type(2)") shouldBe CovidPartialMessages.line2
    }

    s"display the correct direct debit information of ${CovidPartialMessages.directDebit}" in {
      elementText("div > div > p") shouldBe CovidPartialMessages.directDebit
    }
  }

}
