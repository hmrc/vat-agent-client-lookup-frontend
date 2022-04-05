/*
 * Copyright 2022 HM Revenue & Customs
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

import models.agent.ClientVrnModel
import utils.TestUtil

class ClientVrnFormSpec extends TestUtil {

  "Binding a form with invalid data" when {

    "the VRN is missing" should {

      val missingVrn = Map("vrn" -> "")
      val form = ClientVrnForm.form.bind(missingVrn)

      "result in a form with errors" in {
        form.hasErrors shouldBe true
      }

      "throw one error" in {
        form.errors.size shouldBe 1
      }

      "have an error with the correct message" in {
        form.errors.head.message shouldBe "clientVrnForm.error.missing"
      }
    }

    "the VRN is too long" should {

      val longVrn = Map("vrn" -> "223344556677")
      val form = ClientVrnForm.form.bind(longVrn)

      "result in a form with errors" in {
        form.hasErrors shouldBe true
      }

      "throw one error" in {
        form.errors.size shouldBe 1
      }

      "have an error with the correct message" in {
        form.errors.head.message shouldBe "clientVrnForm.error.invalid"
      }
    }

    "the VRN is too short" should {

      val shortVrn = Map("vrn" -> "223")
      val form = ClientVrnForm.form.bind(shortVrn)

      "result in a form with errors" in {
        form.hasErrors shouldBe true
      }

      "throw one error" in {
        form.errors.size shouldBe 1
      }

      "have an error with the correct message" in {
        form.errors.head.message shouldBe "clientVrnForm.error.invalid"
      }
    }

    "the VRN is the correct length but does not validate against the ReferenceChecker library" should {

      val invalidVrn = Map("vrn" -> "123456789")
      val form  = ClientVrnForm.form.bind(invalidVrn)

      "result in a form with errors" in {
        form.hasErrors shouldBe true
      }

      "throw one error" in {
        form.errors.size shouldBe 1
      }

      "have an error with the correct message" in {
        form.errors.head.message shouldBe "clientVrnForm.error.invalid"
      }
    }
  }

  "Binding a form with valid data" should {

    val data = Map("vrn" -> "999969202")
    val form = ClientVrnForm.form.bind(data)

    "result in a form with no errors" in {
      form.hasErrors shouldBe false
    }

    "generate the correct model" in {
      form.value shouldBe Some(ClientVrnModel("999969202"))
    }
  }

  "Binding a form with a valid VRN with spaces in it" should {

    val data = Map("vrn" -> "999 96 92 02  ")
    val form = ClientVrnForm.form.bind(data)

    "result in a form with no errors" in {
      form.hasErrors shouldBe false
    }

    "generate the correct model" in {
      form.value shouldBe Some(ClientVrnModel("999969202"))
    }
  }

  "A form built from a valid model" should {

    "generate the correct mapping" in {
      val model = ClientVrnModel("999969202")
      val form = ClientVrnForm.form.fill(model)
      form.data shouldBe Map("vrn" -> "999969202")
    }
  }
}
