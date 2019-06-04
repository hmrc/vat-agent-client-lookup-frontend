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

package forms

import models.agent.{ChangeDetails, SubmitReturn, ViewCertificate, ViewReturn}
import uk.gov.hmrc.play.test.UnitSpec

class WhatToDoFormSpec extends UnitSpec {
  "Binding a form with valid data" should {
    val validInputValues = Seq(SubmitReturn, ViewReturn, ChangeDetails, ViewCertificate)

    validInputValues.foreach { inputValue =>
      val data = Map("option" -> inputValue.value)
      val form = WhatToDoForm.whatToDoForm.bind(data)

      s"result in a form with no errors for input ${inputValue.value}" in {
        form.hasErrors shouldBe false
      }

      s"generate the correct model for input ${inputValue.value}" in {
        form.value shouldBe Some(inputValue)
      }
    }
  }

  "Binding a form with invalid data" when {

    "the no option has been selected" should {

      val missingOption: Map[String, String] = Map.empty
      val form = WhatToDoForm.whatToDoForm.bind(missingOption)

      "result in a form with errors" in {
        form.hasErrors shouldBe true
      }

      "throw one error" in {
        form.errors.size shouldBe 1
      }
    }
  }

  "A form built from a valid model" should {
    val validInputValues = Seq(SubmitReturn, ViewReturn, ChangeDetails, ViewCertificate)

    validInputValues.foreach { input =>
      s"generate the correct mapping for input: $input" in {
        val form = WhatToDoForm.whatToDoForm.fill(input)
        form.data shouldBe Map("option" -> input.value)
      }
    }

  }
}
