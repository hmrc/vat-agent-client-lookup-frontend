/*
 * Copyright 2024 HM Revenue & Customs
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

package views.helpers

import assets.BaseTestConstants.vrn
import common.SessionKeys
import models.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.helpers.CapturePreferenceHelper

class CapturePreferenceHelperSpec extends ViewBaseSpec {

  val capturePreferenceHelper: CapturePreferenceHelper = inject[CapturePreferenceHelper]

  "CapturePreferenceHelper" when {

    "the agent has entered their email address" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest("GET", "").withSession(SessionKeys.verifiedEmail -> "exampleemail@email.com")
      lazy val testUser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(testGetRequest)
      lazy val view: Html = capturePreferenceHelper("Heading", mockConfig.manageVatCustomerDetailsUrl)(testUser)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct heading" in {
        elementText("a") shouldBe "Heading"
      }

      "link to the redirect location" in {
        element("a").attr("href") shouldBe mockConfig.manageVatCustomerDetailsUrl
      }
    }

    "the agent has selected 'no' to contactPreference" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
        .withSession(SessionKeys.preference -> "no")
      lazy val testUser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(testGetRequest)
      lazy val view: Html = capturePreferenceHelper("Heading", mockConfig.manageVatCustomerDetailsUrl)(testUser)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct heading" in {
        elementText("a") shouldBe "Heading"
      }

      "link to the redirect location" in {
        element("a").attr("href") shouldBe mockConfig.manageVatCustomerDetailsUrl
      }
    }

    "the agent has not entered a contact preference" should {

      lazy val view: Html = capturePreferenceHelper("Heading", mockConfig.manageVatCustomerDetailsUrl)(user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct heading" in {
        elementText("a") shouldBe "Heading"
      }

      "link to the capture preference page" in {
        element("a").attr("href") shouldBe
          controllers.agent.routes.CapturePreferenceController.show().url +
            s"?altRedirectUrl=%2F${mockConfig.manageVatCustomerDetailsUrl.substring(1)}"
      }
    }

  }

}
