/*
 * Copyright 2018 HM Revenue & Customs
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

package forms

import forms.PreferenceForm._
import models.{PreferenceModel, Yes, No}
import play.api.data.FormError
import utils.TestUtil

class PreferenceFormSpec extends TestUtil {

  "The preferenceForm" when {

    val testEmailLocalPart: String = "user"
    val testEmailDomain: String    = "@example.com"
    val testEmail: String          = s"$testEmailLocalPart$testEmailDomain"

    "unbinding the form" when {

      "the email is empty and radio option is yes" should {

        "produce the correct mapping" in {
          val model: PreferenceModel = PreferenceModel(
            yesNo = Yes,
            email = None
          )

          val form = preferenceForm.fill(model)

          form.data shouldBe Map(
            yesNo -> PreferenceForm.yes,
            email -> ""
          )
        }
      }

      "the email is not empty and radio option is no" should {

        "produce the correct mapping" in {
          val model: PreferenceModel = PreferenceModel(
            yesNo = No,
            email = Some(testEmail)
          )

          val form = preferenceForm.fill(model)

          form.data shouldBe Map(
            yesNo -> PreferenceForm.no,
            email -> testEmail
          )
        }
      }
    }

    "no options are selected" should {

      "validate that an option must be entered" in {
        val formWithError = preferenceForm.bind(Map(yesNo -> "", email -> ""))
        formWithError.errors should contain(FormError(yesNo, mandatoryOptionError))
      }
    }

    "the 'No' option is selected" should {

      "validate that no email address is required" in {
        val actual = preferenceForm.bind(Map(yesNo -> yes)).value
        actual shouldBe None
      }
    }

    "the 'Yes' option is selected" should {

      "validate that testEmail is valid" in {
        val actual = preferenceForm.bind(Map(yesNo -> yes, email -> testEmail)).value
        actual shouldBe Some(PreferenceModel(Yes, Some(testEmail)))
      }

      "validate our controlled email where the domain is a valid IP format" in {
        val expectedEmail = testEmailLocalPart + "@111.222.333.444"
        val actual = preferenceForm.bind(Map(yesNo -> yes, email -> expectedEmail)).value
        actual shouldBe Some(PreferenceModel(Yes, Some(expectedEmail)))
      }

      "validate our controlled email where the local-part contain only numbers" in {
        val expectedEmail = "1234567890" + testEmailDomain
        val actual = preferenceForm.bind(Map(yesNo -> yes, email -> expectedEmail)).value
        actual shouldBe Some(PreferenceModel(Yes, Some(expectedEmail)))
      }

      "validate our controlled email where the local-part contains legal special characters" in {
        val expectedEmail = "#!$%&'*+-/=?^_`{}|~" + testEmailDomain
        val actual = preferenceForm.bind(Map(yesNo -> yes, email -> expectedEmail)).value
        actual shouldBe Some(PreferenceModel(Yes, Some(expectedEmail)))
      }

      "validate our controlled email when there are separations in the domain name" in {
        val expectedEmail = testEmailLocalPart + "@a.a-a.com"
        val actual = preferenceForm.bind(Map(yesNo -> yes, email -> expectedEmail)).value
        actual shouldBe Some(PreferenceModel(Yes, Some(expectedEmail)))
      }

      "validate our controlled email where the local part contains capitals" in {
        val expectedEmail = "TEST" + testEmailDomain
        val actual = preferenceForm.bind(Map(yesNo -> yes, email -> expectedEmail)).value
        actual shouldBe Some(PreferenceModel(Yes, Some(expectedEmail)))
      }

      "validate our controlled email where the domain contains capitals" in {
        val expectedEmail = testEmailLocalPart + "@TEST.COM"
        val actual = preferenceForm.bind(Map(yesNo -> yes, email -> expectedEmail)).value
        actual shouldBe Some(PreferenceModel(Yes, Some(expectedEmail)))
      }

      "validate that data has been entered" in {
        val formWithError = preferenceForm.bind(Map(yesNo -> yes, email -> ""))
        formWithError.errors should contain(FormError("email", mandatoryEmailError))
      }

      "validate that invalid email fails" in {
        val formWithError = preferenceForm.bind(Map(yesNo -> yes, email -> "invalid"))
        formWithError.errors should contain(FormError("email", invalidEmailError))
      }

      "validate that invalid email fails where the domain contains 2 dots" in {
        val testEmail = testEmailLocalPart + "@a..b"
        val formWithError = preferenceForm.bind(Map(yesNo -> yes, email -> testEmail))
        formWithError.errors should contain(FormError("email", invalidEmailError))
      }

      "validate that invalid email fails where domain does not contain dots" in {
        val testEmail = testEmailLocalPart + "@a"
        val formWithError = preferenceForm.bind(Map(yesNo -> yes, email -> testEmail))
        formWithError.errors should contain(FormError("email", invalidEmailError))
      }

      "validate that invalid email fails where the domain contains multiple @ symbols" in {
        val testEmail = testEmailLocalPart + "a@a@"
        val formWithError = preferenceForm.bind(Map(yesNo -> yes, email -> testEmail))
        formWithError.errors should contain(FormError("email", invalidEmailError))
      }

      "validate that invalid email fails where local-part contains illegal characters without quotes" in {
        val testEmail = "this is\"not\\allowed" + testEmailDomain
        val formWithError = preferenceForm.bind(Map(yesNo -> yes, email -> testEmail))
        formWithError.errors should contain(FormError("email", invalidEmailError))
      }

      "validate that invalid email fails where unicode chars included in local-part" in {
        val testEmail = "あいうえお" + testEmailDomain
        val formWithError = preferenceForm.bind(Map(yesNo -> yes, email -> testEmail))
        formWithError.errors should contain(FormError("email", invalidEmailError))
      }

      "validate that invalid email fails where local-part email not included" in {
        val testEmail = testEmailDomain
        val formWithError = preferenceForm.bind(Map(yesNo -> yes, email -> testEmail))
        formWithError.errors should contain(FormError("email", invalidEmailError))
      }

      "validate that invalid email fails where encoded html included" in {
        val testEmail = "Joe Smith <" + testEmailLocalPart + testEmailDomain + ">"
        val formWithError = preferenceForm.bind(Map(yesNo -> yes, email -> testEmail))
        formWithError.errors should contain(FormError("email", invalidEmailError))
      }

      "validate that email does not exceed max length" in {
        val exceed = preferenceForm.bind(Map(yesNo -> yes, email -> ("a" * (maxLength + 1)))).errors
        exceed should contain(FormError("email", emailLengthError))
        exceed.seq.size shouldBe 1
      }

      "validate that email allows max length" in {
        val errors = preferenceForm.bind(Map(yesNo -> yes, email -> ("a" * maxLength))).errors
        errors should not contain FormError("email", emailLengthError)
      }
    }
  }
}