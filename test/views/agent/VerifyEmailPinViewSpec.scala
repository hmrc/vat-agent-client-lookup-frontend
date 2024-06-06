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

import forms.PasscodeForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.agent.VerifyEmailPinView
import views.ViewBaseSpec

class VerifyEmailPinViewSpec extends ViewBaseSpec {

  val injectedView: VerifyEmailPinView = inject[VerifyEmailPinView]
  val testEmail: String = "test@email.com"

  "The VerifyEmailPiView page" should {

    lazy val view: Html = injectedView(testEmail, PasscodeForm.form)(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe "Enter code to confirm your email address - Your client’s VAT details - GOV.UK"
    }

    "have the correct heading" in {
      elementText("h1") shouldBe "Enter code to confirm your email address"
    }

    "have a back link" which {

      "has the correct text" in {
        elementText(".govuk-back-link") shouldBe "Back"
      }

      "has the correct destination" in {
        element(".govuk-back-link").attr("href") shouldBe controllers.agent.routes.CapturePreferenceController.show().url
      }
    }

    "have the correct first paragraph" in {
      elementText("#content p:nth-of-type(1)") shouldBe s"We have sent a code to: $testEmail"
    }

    "have the correct inset text" in {
      elementText(".govuk-inset-text") shouldBe "Open a new tab or window if you need to access your emails online."
    }

    "have the correct subheading for the input box" in {
      elementText("label[for = passcode]") shouldBe "Confirmation code"
    }

    "have the correct form hint" in {
      elementText(".govuk-hint") shouldBe "For example, DNCLRK"
    }

    "have the correct progressive disclosure text" in {
      elementText(".govuk-details__summary") shouldBe "I have not received the email"
    }

    "have the correct first paragraph inside of the progressive disclosure" in {
      elementText("details div p:nth-child(1)") shouldBe
        "The email may take a few minutes to arrive. Its subject line is: ‘Confirm your email address - VAT account’."
    }

    "have the correct second paragraph inside of the progressive disclosure" in {
      elementText("details div p:nth-child(2)") shouldBe "Check your spam or junk folder. " +
        "If the email has still not arrived, you can request a new code or provide another email address."
    }

    "have a link to resend the passcode" which {

      "has the correct text" in {
        elementText("details div p:nth-child(2) a:nth-child(1)") shouldBe "request a new code"
      }

      "has the correct destination" in {
        element("details div p:nth-child(2) a:nth-child(1)").attr("href") shouldBe
          controllers.agent.routes.VerifyEmailPinController.requestPasscode.url
      }
    }

    "have a link to enter a new email address" which {

      "has the correct text" in {
        elementText("details div p:nth-child(2) a:nth-child(2)") shouldBe "provide another email address"
      }

      "has the correct destination" in {
        element("details div p:nth-child(2) a:nth-child(2)").attr("href") shouldBe
          controllers.agent.routes.CapturePreferenceController.show().url
      }
    }

    "have a continue button" which {

      "has the correct text" in {
        elementText(".govuk-button") shouldBe "Continue"
      }

      "has the prevent double click attribute" in {
        element(".govuk-button").hasAttr("data-prevent-double-click") shouldBe true
      }
    }
  }

  "The VerifyEmailPinView when there are errors in the form" should {

    lazy val view: Html = injectedView(testEmail, PasscodeForm.form.bind(Map("passcode" -> "badthings")))(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe "Error: Enter code to confirm your email address - Your client’s VAT details - GOV.UK"
    }

    "have an error summary" which {

      "has the correct heading" in {
        elementText(".govuk-error-summary__title") shouldBe "There is a problem"
      }

      "has the correct error message" in {
        elementText(".govuk-error-summary__body") shouldBe "Enter the 6 character confirmation code"
      }

      "has a link to the input with the error" in {
        element(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#passcode"
      }
    }

    "have the correct error notification text above the input box" in {
      elementText(".govuk-error-message") shouldBe "Error: Enter the 6 character confirmation code"
    }
  }
}
