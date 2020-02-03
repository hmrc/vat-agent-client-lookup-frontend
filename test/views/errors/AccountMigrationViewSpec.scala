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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.AccountMigrationView

class AccountMigrationViewSpec extends ViewBaseSpec {

  val injectedView: AccountMigrationView = inject[AccountMigrationView]

  "Rendering the Account Migration error page" should {

    lazy val view = injectedView()(request, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      document.title shouldBe "Your client’s VAT details - Your client’s VAT details - GOV.UK"
    }

    "have the correct heading" in {
      elementText("h1") shouldBe "You cannot make changes for that client’s business right now"
    }

    "have the correct main paragraph" in {
      elementText("#content article p:nth-of-type(1)") shouldBe
        "This is because the business’s Making Tax Digital account is being set up - this can take up to 72 hours."
    }

    "have the correct second paragraph" in {
      elementText("#content article p:nth-of-type(2)") shouldBe "Check back again later."
    }

    "have the correct link for making changes to a different client which" should {

      "have the correct text" in {
        elementText("#content article a") shouldBe "Make changes for a different client"
      }

      "have the correct href" in {
        element("#content article a").attr("href") shouldBe "/vat-through-software/representative/client-vat-number"
      }
    }
  }
}
