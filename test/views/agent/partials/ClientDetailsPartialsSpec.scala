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
import assets.messages.partials.ClientDetailsPartialMessages
import common.SessionKeys
import models.User
import views.html.agent.partials.ClientDetailsPartials

class ClientDetailsPartialsSpec extends ViewBaseSpec {

  val clientDetailsPartials: ClientDetailsPartials = injector.instanceOf[ClientDetailsPartials]

  "ClientDetailsPartials" when {

    "when the user has no contact preference in session" should {

      lazy val view = clientDetailsPartials()(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct card heading of ${ClientDetailsPartialMessages.heading}" in {
        elementText(".heading-medium") shouldBe ClientDetailsPartialMessages.heading
      }

      s"display the correct line 1 of ${ClientDetailsPartialMessages.paragraphOne}" in {
        elementText("p") shouldBe ClientDetailsPartialMessages.paragraphOne
      }

      s"display the correct link text of ${ClientDetailsPartialMessages.linkText}" in {
        elementText("a") shouldBe ClientDetailsPartialMessages.linkText
      }

      s"display the correct link of ${controllers.agent.routes.CapturePreferenceController.show().url}" in {
        element("a").attr("href") shouldBe
          controllers.agent.routes.CapturePreferenceController.show().url +
            s"?altRedirectUrl=%2F${mockConfig.manageVatCustomerDetailsUrl.substring(1)}"
      }
    }


    "when the user has a valid agent email in session" should {

      lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
        .withSession(SessionKeys.verifiedAgentEmail -> "exampleemail@email.com")

      lazy val testuser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(testGetRequest)

      lazy val view = clientDetailsPartials()(messages, mockConfig, testuser)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct card heading of ${ClientDetailsPartialMessages.heading}" in {
        elementText(".heading-medium") shouldBe ClientDetailsPartialMessages.heading
      }

      s"display the correct line 1 of ${ClientDetailsPartialMessages.paragraphOne}" in {
        elementText("p") shouldBe ClientDetailsPartialMessages.paragraphOne
      }

      s"display the correct link text of ${ClientDetailsPartialMessages.linkText}" in {
        elementText("a") shouldBe ClientDetailsPartialMessages.linkText
      }

      s"display the correct link of ${mockConfig.manageVatCustomerDetailsUrl}" in {
        element("a").attr("href") shouldBe mockConfig.manageVatCustomerDetailsUrl
      }
    }

    "when the user has email notification in session as no" should {

      lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
        .withSession(SessionKeys.preference -> "no")

      lazy val testuser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(testGetRequest)

      lazy val view = clientDetailsPartials()(messages, mockConfig, testuser)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct card heading of ${ClientDetailsPartialMessages.heading}" in {
        elementText(".heading-medium") shouldBe ClientDetailsPartialMessages.heading
      }

      s"display the correct line 1 of ${ClientDetailsPartialMessages.paragraphOne}" in {
        elementText("p") shouldBe ClientDetailsPartialMessages.paragraphOne
      }

      s"display the correct link text of ${ClientDetailsPartialMessages.linkText}" in {
        elementText("a") shouldBe ClientDetailsPartialMessages.linkText
      }

      s"display the correct link of ${mockConfig.manageVatCustomerDetailsUrl}" in {
        element("a").attr("href") shouldBe mockConfig.manageVatCustomerDetailsUrl
      }
    }
  }

}
