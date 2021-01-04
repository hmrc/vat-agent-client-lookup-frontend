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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.NotSignedUpView

class NotSignedUpViewSpec extends ViewBaseSpec {

  val injectedView: NotSignedUpView = inject[NotSignedUpView]

  "Rendering the Not Signed Up error page" should {

    lazy val view = injectedView()(request, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      document.title shouldBe "Your client’s VAT details - Your client’s VAT details - GOV.UK"
    }

    "have the correct heading" in {
      elementText("h1") shouldBe "The business has not signed up to Making Tax Digital for VAT"
    }

    "have the correct main paragraph" in {
      elementText("#content article p:nth-of-type(1)") shouldBe
        "If you want to make any changes to your client’s details, you’ll need to sign them up to Making Tax Digital for VAT (opens in a new tab)."
    }

    "have the correct second paragraph" should {

      "have the correct text" in {
        elementText("#content article p:nth-of-type(2)") shouldBe
          "You can sign out and use your existing HMRC online services account to submit VAT Returns and manage your client’s business details."
      }

      "have the correct redirect url to the classic services sign in page" in {
        element("#use-existing-hmrc-service-account").attr("href") shouldBe "/classic-services-sign-in"
      }
    }

    "have the correct link for making changes to a different client which" should {

      "have the correct text" in {
        elementText("#content article > a") shouldBe "Make changes for a different client"
      }

      "have the correct href" in {
        element("#content article > a").attr("href") shouldBe "/vat-through-software/representative/client-vat-number"
      }
    }
  }
}
