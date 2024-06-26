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

package views.agent

import assets.messages.{BaseMessages, ClientVrnPageMessages => viewMessages}
import forms.ClientVrnForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.SelectClientVrnView

class SelectClientVrnViewSpec extends ViewBaseSpec {

  val injectedView: SelectClientVrnView = inject[SelectClientVrnView]

  val headerName = ".govuk-header__service-name"
  val signOutLink = ".hmrc-sign-out-nav__link"

  "Rendering the Select Client VRN page" when {

    "there are no errors in the form" should {

      lazy val view = injectedView(ClientVrnForm.form)(request, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct document title of '${viewMessages.title}'" in {
        document.title shouldBe (viewMessages.title + " - Your client’s VAT details - GOV.UK")
      }

      s"have the correct service name" in {
        elementText(headerName) shouldBe BaseMessages.agentServiceName
      }

      s"have the correct page heading of '${viewMessages.heading}'" in {
        elementText("h1") shouldBe viewMessages.heading
      }

      s"have the correct form hint of '${viewMessages.hint}'" in {
        elementText(".govuk-hint") shouldBe s"${viewMessages.p1} ${viewMessages.hint}"
      }

      s"have an input box for the VRN" in {
        elementText("label[for = vrn]") shouldBe viewMessages.label
      }

      s"have a submit button with the text '${BaseMessages.continue}'" in {
        elementText("button") shouldBe BaseMessages.continue
      }

      "has the correct form action" in {
        element("form").attr("action") shouldBe controllers.agent.routes.SelectClientVrnController.submit.url
      }

      "have the sign out link in the page header" in {
        elementText(signOutLink) shouldBe "Sign out"
      }

      "redirect to the feedback survey on sign out" in {
        element(signOutLink).attr("href") shouldBe
          controllers.routes.SignOutController.signOut(feedbackOnSignOut = true).url
      }
    }

    "there are errors in the form" should {

      lazy val view = injectedView(ClientVrnForm.form.bind(Map("vrn" -> "9")))(request, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe "Error: " + (viewMessages.title + " - Your client’s VAT details - GOV.UK")
      }

      "have a form error box" which {

        "has the correct heading" in {
          elementText(".govuk-error-summary__title") shouldBe viewMessages.formErrorHeading
        }

        "has the correct error message" in {
          elementText(".govuk-error-summary__body > ul > li > a") shouldBe viewMessages.formErrorInvalidVrn
        }
      }

      "have the correct error notification text above the input box" in {
        elementText("#vrn-error") shouldBe s"Error: ${viewMessages.formErrorInvalidVrn}"
      }

      "retain the value that the user has entered " in {
        element(".govuk-input").attr("value") shouldBe "9"
      }
    }
  }
}
