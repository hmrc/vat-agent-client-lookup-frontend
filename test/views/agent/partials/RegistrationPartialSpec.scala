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

import assets.messages.partials.RegistrationPartialMessages
import models.{Deregistered, Registered}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.partials.registrationPartial

class RegistrationPartialSpec extends ViewBaseSpec {

  "Rendering the partial" when {

    "client is registered" should {

      "display a section for cancelling registration" which {

        lazy val view = registrationPartial(Registered)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        s"should have the correct title of ${RegistrationPartialMessages.cancelRegistrationTitle}" in {
          elementText("h3") shouldBe RegistrationPartialMessages.cancelRegistrationTitle
        }

        s"link to ${mockConfig.cancelRegistrationUrl}" in {
          element("h3 > a").attr("href") shouldBe mockConfig.cancelRegistrationUrl
        }

        s"have correct content of ${RegistrationPartialMessages.cancelRegistrationContent}" in {
          elementText("p") shouldBe RegistrationPartialMessages.cancelRegistrationContent
        }
      }
    }

    "client is not registered" should {

      lazy val view = registrationPartial(Deregistered)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display nothing" in {
        document.select(".card") shouldBe empty
      }
    }
  }
}
