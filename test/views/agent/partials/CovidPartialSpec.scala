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

  "The covid partial pre end of June 2020" should {

    lazy val view = covid(postCovidDeadline = false)(messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct card heading" in {
      elementText("strong") shouldBe CovidPartialMessages.headingPreEnd
    }

    "display the correct line 1" in {
      elementText("li:nth-of-type(1)") shouldBe CovidPartialMessages.line1
    }

    "display the correct line 2" which {

      "has the correct text" in {
        elementText("li:nth-of-type(2)") shouldBe CovidPartialMessages.line2
      }

      "has the correct link text" in {
        elementText("a") shouldBe CovidPartialMessages.line2link
      }

      "has the correct link location" in {
        element("a").attr("href") shouldBe mockConfig.difficultiesPayingUrl
      }
    }

    "display the correct line 3" in {
      elementText("li:nth-of-type(3)") shouldBe CovidPartialMessages.line3
    }

    "display the correct line 4" in {
      elementText("li:nth-of-type(4)") shouldBe CovidPartialMessages.line4
    }
  }

  "The covid partial post end of June 2020" should {

    lazy val view = covid(postCovidDeadline = true)(messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct card heading" in {
      elementText("strong") shouldBe CovidPartialMessages.headingPostEnd
    }
  }
}
