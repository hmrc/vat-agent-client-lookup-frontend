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

package views.agent

import assets.messages.{DirectDebitInterruptPageMessages => viewMessages}
import forms.DDInterruptForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.DirectDebitInterruptView

class DirectDebitInterruptViewSpec extends ViewBaseSpec {

  val injectedView: DirectDebitInterruptView = inject[DirectDebitInterruptView]

  "The DD interrupt screen for users" when {

    "there are no errors in the form" should {

      lazy val view = injectedView(DDInterruptForm.form)(request, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe (viewMessages.title + " - Your client’s VAT details - GOV.UK")
      }

      "have the correct page heading" in {
        elementText("h1") shouldBe viewMessages.title
      }

      "have the correct first paragraph" in {
        elementText("#content > article p:nth-of-type(1)") shouldBe viewMessages.para1
      }

      "have the correct second paragraph" in {
        elementText("#content > article p:nth-of-type(2)") shouldBe viewMessages.para2
      }

      "have the correct bold text in the second paragraph" in {
        elementText("#content strong") shouldBe viewMessages.boldText
      }

      "have the correct checkbox label" in {
        elementText("label[for=checkbox]") shouldBe viewMessages.checkboxLabel
      }

      "have the correct form action" in {
        element("form").attr("action") shouldBe controllers.agent.routes.DDInterruptController.submit.url
      }

      "have a button" which {

        "has the correct text" in {
          elementText(".button") shouldBe viewMessages.buttonText
        }

        "submits the form" in {
          element(".button").attr("type") shouldBe "submit"
        }
      }
    }

    "there is an error in the form" should {

      lazy val view = injectedView(DDInterruptForm.form.bind(Map("checkbox" -> "")))(request, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe s"Error: ${viewMessages.title} - Your client’s VAT details - GOV.UK"
      }

      "have the error summary" which {

        "has the correct heading" in {
          elementText("#error-summary-heading") shouldBe viewMessages.formErrorHeading
        }

        "has the correct error message" in {
          elementText("#checkbox-error-summary") shouldBe viewMessages.formErrorText
        }

        "has the correct link in the error message" in {
          element("#checkbox-error-summary").attr("href") shouldBe "#checkbox"
        }
      }

      "has the error message above the checkbox" in {
        elementText(".error-message") shouldBe s"Error: ${viewMessages.formErrorText}"
      }
    }
  }
}
