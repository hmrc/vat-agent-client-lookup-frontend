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

package views.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.data.Forms.nonEmptyText
import play.twirl.api.Html
import views.html.agent.VerifyEmailPinView
import views.ViewBaseSpec

class VerifyEmailPinSpec extends ViewBaseSpec {

val injectedView: VerifyEmailPinView = inject[VerifyEmailPinView]
val testForm: Form[String] = Form("passcode" -> nonEmptyText) //TODO - replace with real form

"The VerifyEmailPiView page" should {

  lazy val view: Html = injectedView(testEmail, testForm)(user, messages, mockConfig)
  lazy implicit val document: Document = Jsoup.parse(view.body)

  "have the correct document title" in {
  document.title shouldBe "Enter code to confirm your email address - Business tax account - GOV.UK"
}

  "have the correct heading" in {
  elementText("h1") shouldBe "Enter code to confirm your email address"
}

  "have a back link" which {

  "has the correct text" in {
  elementText(".link-back") shouldBe "Back"
}

  "has the correct destination" in {
  element(".link-back").attr("href") shouldBe controllers.agent.routes.CapturePreferenceController.show().url
}
}

  "have the correct first paragraph" in {
  elementText("#content article p:nth-of-type(1)") shouldBe "We have sent a code to: $testEmail"
}
  "have the correct panel paragraph" in {
  elementText("p.panel") shouldBe "Open a new tab or window if you need to access your emails online."
}

  "have the correct subheading for the form" in {
  elementText("h2") shouldBe "Confirmation code"
}

  "have the correct form hint" in {
  elementText("#form-hint") shouldBe "For example, DNCLRK"
}

  "have the correct progressive disclosure text" in {
  elementText(".summary") shouldBe "I have not received the email"
}

  "have the correct first paragraph inside of the progressive disclosure" in {
  elementText("details div p:nth-child(1)") shouldBe
  "This email might take a few minutes to arrive. Its subject line is: ‘Confirm your email address - VAT account'."
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
  element("details div p:nth-child(2) a:nth-child(1)").attr("href") shouldBe "#"
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

  "have a button with the correct text" in {
  elementText(".button") shouldBe "Continue"
}
}

  "The VerifyEmailPinView when there are errors in the form" should {

  val errorForm = testForm.withError(FormError("passcode", "Test error")) //TODO - replace with real form
  lazy val view: Html = injectedView(testEmail, errorForm)(user, messages, mockConfig)
  lazy implicit val document: Document = Jsoup.parse(view.body)

  "have the correct document title" in {
  document.title shouldBe "Error: Enter code to confirm your email address - Business tax account - GOV.UK"
}

  "have an error summary" which {

  "has the correct heading" in {
  elementText("#error-summary-heading") shouldBe "There is a problem"
}

  "has the correct error message" in {
  elementText("#passcode-error-summary") shouldBe "Test error"
}

  "has a link to the input with the error" in {
  element("#passcode-error-summary").attr("href") shouldBe "#passcode"
}
}

  "have the correct error notification text above the input box" in {
  elementText(".error-message") shouldBe "Error: Test error"
}
}
}
