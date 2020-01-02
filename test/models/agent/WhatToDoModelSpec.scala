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

package models.agent

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class WhatToDoModelSpec extends UnitSpec {

  val inputValues = Seq(SubmitReturn, ViewReturn, ViewCertificate, ChangeDetails)

  inputValues.foreach { input =>
    val validJson = Json.obj(
      "whatToDo" -> input.value
    )

    s"When trying to read a ${input.value}" should {
      "return a successfully parsed model" in {
        validJson.as[WhatToDoModel] shouldBe input
      }
    }

    s"When trying to write to json for a ${input.value}" should {
      "successfully write to json" in {
        Json.toJson(input) shouldBe validJson
      }
    }
  }
}
