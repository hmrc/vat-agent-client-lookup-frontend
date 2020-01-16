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

import assets.BaseTestConstants.vrn
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec
import assets.messages.partials.OptOutForMTDVATMessages
import common.{MandationStatus, SessionKeys}
import models.User

class OptOutForMTDVATPartialSpec extends ViewBaseSpec {

  "OptOutForMTDVATPartial view" when {

    "with a mandated status" when {

      "agent has not entered their contact preference" should {

        lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
        lazy val view = views.html.agent.partials.optOutForMTDVATPartial("MTDfB Mandated")(messages, mockConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        s"have the correct title of ${OptOutForMTDVATMessages.title}" in {
          elementText(".heading-small") shouldBe OptOutForMTDVATMessages.title
        }

        s"have the correct link of ${controllers.agent.routes.CapturePreferenceController.show().url}" in {
          element("a").attr("href") shouldBe controllers.agent.routes.CapturePreferenceController.show().url
        }

        s"have the correct card information of ${OptOutForMTDVATMessages.description}" in {
          elementText("p") shouldBe OptOutForMTDVATMessages.description
        }
      }

      "agent has entered their contact preference" should {

        lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
          .withSession(SessionKeys.verifiedAgentEmail -> "exampleemail@email.com")
        lazy val testUser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(testGetRequest)
        lazy val view = views.html.agent.partials.optOutForMTDVATPartial("MTDfB Mandated")(messages, mockConfig, testUser)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        s"have the correct title of ${OptOutForMTDVATMessages.title}" in {
          elementText(".heading-small") shouldBe OptOutForMTDVATMessages.title
        }

        s"have the correct link of ${mockConfig.optOutMtdVatUrl}" in {
          element("a").attr("href") shouldBe mockConfig.optOutMtdVatUrl
        }

        s"have the correct card information of ${OptOutForMTDVATMessages.description}" in {
          elementText("p") shouldBe OptOutForMTDVATMessages.description
        }
      }
    }

    "with a mandation status of nonMTDfB" should {

      lazy val view = views.html.agent.partials.optOutForMTDVATPartial(MandationStatus.nonMTDfB)(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not be displayed" in {

        document.select(".card") shouldBe empty

      }
    }
  }
}
