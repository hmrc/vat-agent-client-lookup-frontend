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

package connectors.httpParsers

import assets.CustomerDetailsTestConstants._
import connectors.httpParsers.CustomerDetailsHttpParser.CustomerDetailsReads.read
import models.errors._
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtil

class CustomerDetailsHttpParserSpec extends TestUtil {

  val successBadJson: JsObject = Json.obj("firstName" -> 1)
  val errorModel: UnexpectedError = UnexpectedError(Status.BAD_REQUEST, "Error Message")

  "The CustomerDetailsHttpParser" when {

    "the http response status is OK with valid Json" should {

      "return a CustomerDetailsModel" in {
        read("", "", HttpResponse(Status.OK, allInfoJson.toString())) shouldBe Right(customerDetailsAllInfo)
      }
    }

    "the http response status is OK with invalid Json" should {

      "return an UnexpectedError" in {
        read("", "", HttpResponse(Status.OK, successBadJson.toString())) shouldBe
          Left(UnexpectedError(Status.INTERNAL_SERVER_ERROR, "Invalid Json"))
      }
    }

    "the http response status is PRECONDITION_FAILED" should {

      "return an Migration error" in {
        read("", "", HttpResponse(Status.PRECONDITION_FAILED, "")) shouldBe
          Left(Migration)
      }
    }

    "the http response status is NOT_FOUND" should {

      "return an NotSignedUp error" in {
        read("", "", HttpResponse(Status.NOT_FOUND, "")) shouldBe
          Left(NotSignedUp)
      }
    }

    "the http response status is unexpected" should {

      "return an UnexpectedError with the status and response body" in {
        val httpResponse = HttpResponse(Status.BAD_REQUEST, "")
        read("", "", httpResponse) shouldBe Left(UnexpectedError(Status.BAD_REQUEST, httpResponse.body))
      }
    }
  }
}
