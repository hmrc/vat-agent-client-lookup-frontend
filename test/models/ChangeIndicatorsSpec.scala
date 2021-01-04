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

package models

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.play.test.UnitSpec

class ChangeIndicatorsSpec extends UnitSpec {

  val jsonAllTrue: JsObject = Json.obj("deregister" -> true, "PPOBDetails" -> true)
  val jsonAllFalse: JsObject = Json.obj("deregister" -> false, "PPOBDetails" -> false)
  val modelAllTrue: ChangeIndicators = ChangeIndicators(deregister = true, PPOBDetails = true)
  val modelAllFalse: ChangeIndicators = ChangeIndicators(deregister = false, PPOBDetails = false)

  "ChangeindicatorsModel" should {

    "deserialize from JSON" when {

      "all fields are true" in {
        jsonAllTrue.as[ChangeIndicators] shouldBe modelAllTrue
      }

      "all fields are false" in {
        jsonAllFalse.as[ChangeIndicators] shouldBe modelAllFalse
      }
    }

    "serialize to JSON" when {

      "all fields are true" in {
        Json.toJson(modelAllTrue) shouldBe jsonAllTrue
      }

      "all fields are false" in {
        Json.toJson(modelAllFalse) shouldBe jsonAllFalse
      }
    }
  }
}
