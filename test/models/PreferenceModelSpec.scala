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

package models

import play.api.libs.json.Json
import utils.TestUtil

class PreferenceModelSpec extends TestUtil {

  val testEmail: String = "test@example.com"

  "PreferenceModel.isValid" should {

    "return true" when {

      "given a Yes and an amount" in {
        PreferenceModel(Yes, Some(testEmail)).isValid shouldBe true
      }

      "given a No and an amount" in {
        PreferenceModel(No, Some(testEmail)).isValid shouldBe true
      }

      "given a No and no amount" in {
        PreferenceModel(No, None).isValid shouldBe true
      }
    }

    "return false" when {

      "given a Yes and no amount" in {
        PreferenceModel(Yes, None).isValid shouldBe false
      }
    }
  }

  "PreferenceModel.format" should {

    "serialize to the correct JSON" in {
      Json.toJson(PreferenceModel(Yes, Some(testEmail))) shouldBe
        Json.obj(
          "yesNo" -> Json.obj(YesNo.id -> true),
          "email" -> testEmail
        )
    }

    "deserialize from JSON correctly" in {
      Json.obj(
        "yesNo" -> Json.obj(YesNo.id -> true),
        "email" -> testEmail
      ).as[PreferenceModel] shouldBe PreferenceModel(Yes,Some(testEmail))

    }
  }
}