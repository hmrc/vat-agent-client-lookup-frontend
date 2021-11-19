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

package forms

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class DDInterruptFormSpec extends AnyWordSpecLike with Matchers {

  "Binding the DD Interrupt Form" when {

    "valid data is given" should {

      val form = DDInterruptForm.form.bind(Map("checkbox" -> "true"))

      "result in a form with no errors" in {
        form.hasErrors shouldBe false
      }

      "generate the correct value" in {
        form.value shouldBe Some(true)
      }
    }

    "no data is given" should {

      val form = DDInterruptForm.form.bind(Map("checkbox" -> "false"))

      "result in a form with errors" in {
        form.hasErrors shouldBe true
      }

      "throw one error" in {
        form.errors.size shouldBe 1
      }

      "have an error with the correct message key" in {
        form.errors.head.message shouldBe "directDebitInterrupt.formError"
      }
    }
  }
}
