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

package views.agent

import forms.PreferenceForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.ViewBaseSpec

class CapturePreferenceViewSpec extends ViewBaseSpec {

  object Selectors {
    val pageHeading         = "#content h1"
    val subtext             = "fieldset > p"
    val backLink            = "#content > article > a"
    val emailQuestionText   = "#hiddenContent > label > span.form-field"
    val emailHintText       = "#hiddenContent > label > span.form-hint"
    val form                = "form"
    val emailField          = "#email"
    val continueButton      = "button"
    val errorSummary        = "#error-summary-heading"
    val radioOptionYes      = "#yes_no-yes"
    val radioOptionNo       = "#yes_no-no"
    val radioOptionYesLabel = "#label-yes_no-yes"
    val radioOptionNoLabel  = "#label-yes_no-no"
    val emailFormGroup      = "#hiddenContent"
  }

  "Rendering the capture preference page" when {

    "the form has no errors" when {

      "the user has no radio option selected" should {

        lazy val view: Html = views.html.agent.capturePreference(preferenceForm)(request, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "Would you like to receive email notifications of any changes you make? - Client’s VAT details - GOV.UK"
        }

        "have the correct page heading" in {
          elementText(Selectors.pageHeading) shouldBe
            "Would you like to receive email notifications of any changes you make?"
        }

        "have the correct subtext" in {
          elementText(Selectors.subtext) shouldBe
            "We will also send a secure message to your client’s business tax account."
        }

        "have the preference form with the correct form action" in {
          element(Selectors.form).attr("action") shouldBe "/vat-through-software/representative/email-notification"
        }

        "have the yes radio option" in {
          element(Selectors.radioOptionYes).attr("value") shouldBe "yes"
        }

        "have the no radio option" in {
          element(Selectors.radioOptionNo).attr("value") shouldBe "no"
        }

        "have the correct yes radio option label text" in {
          elementText(Selectors.radioOptionYesLabel) shouldBe "Yes"
        }

        "have the correct no radio option label text" in {
          elementText(Selectors.radioOptionNoLabel) shouldBe "No"
        }

        "have the email form section hidden" in {
          element(Selectors.emailFormGroup).hasClass("js-hidden") shouldBe true
        }

        "have the correct email question text" in {
          elementText(Selectors.emailQuestionText) shouldBe "What is your email address?"
        }

        "have the correct email hint text" in {
          elementText(Selectors.emailHintText) shouldBe
            "We will only use this to send you a confirmation of any changes you make"
        }

        "have the continue button" in {
          elementText(Selectors.continueButton) shouldBe "Continue"
        }
      }

      "the user has the 'Yes' radio option selected" should {

        lazy val view: Html =
          views.html.agent.capturePreference(preferenceForm.bind(Map(yesNo -> "yes")))(request, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the 'Yes' radio option checked" in {
          element(Selectors.radioOptionYes).attr("checked") shouldBe "checked"
        }

        "have the email form section displayed" in {
          element(Selectors.emailFormGroup).hasClass("js-hidden") shouldBe false
        }
      }

      "the user has the 'No' radio option selected" should {

        lazy val view: Html =
          views.html.agent.capturePreference(preferenceForm.bind(Map(yesNo -> "no")))(request, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the 'No' radio option checked" in {
          element(Selectors.radioOptionNo).attr("checked") shouldBe "checked"
        }

        "have the email form section hidden" in {
          element(Selectors.emailFormGroup).hasClass("js-hidden") shouldBe true
        }
      }
    }

    "the form has an option error" should {
      lazy val view =
        views.html.agent.capturePreference(preferenceForm.bind(Map(yesNo -> "")))(request, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe "Error: Would you like to receive email notifications of any changes you make? - Client’s VAT details - GOV.UK"
      }

      "display the error summary" in {
        element(Selectors.errorSummary).text() shouldBe "There is a problem"
      }
    }

    "the form has an email error" should {
      lazy val view = views.html.agent.capturePreference(
        preferenceForm.bind(Map(yesNo -> yes, email -> "invalid")))(request, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe "Error: Would you like to receive email notifications of any changes you make? - Client’s VAT details - GOV.UK"
      }

      "display the error summary" in {
        element(Selectors.errorSummary).text() shouldBe "There is a problem"
      }
    }
  }
}
