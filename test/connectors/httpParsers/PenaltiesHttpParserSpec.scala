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

package connectors.httpParsers

import assets.PenaltiesConstants.penaltiesSummaryAsModel
import connectors.httpParsers.PenaltiesHttpParser.PenaltiesReads
import models.errors.UnexpectedError
import models.penalties.PenaltiesSummary
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HttpResponse

class PenaltiesHttpParserSpec extends AnyWordSpecLike with Matchers {

  "PenaltiesHttpParser" when {

    "the http response status is 200 OK" should {
      val jsonResponse = Json.obj(
        "noOfPoints" -> 3,
        "noOfEstimatedPenalties" -> 2,
        "noOfCrystalisedPenalties" -> 1,
        "estimatedPenaltyAmount" -> 123.45,
        "crystalisedPenaltyAmountDue" -> 54.32,
        "hasAnyPenaltyData" -> true
      )
      val httpResponse = HttpResponse(Status.OK, jsonResponse.toString())
      val expected = Right(penaltiesSummaryAsModel)
      val result = PenaltiesReads.read("", "", httpResponse)

      "return a PenaltiesSummary model" in {
        result shouldBe expected
      }
    }

    "the http response status is 404 NOT_FOUND" should {
      val httpResponse = HttpResponse(Status.NOT_FOUND, "")
      val expected = Right(PenaltiesSummary.empty)
      val result = PenaltiesReads.read("", "", httpResponse)

      "return an empty model" in {
        result shouldEqual expected
      }
    }

    "the http response status is 400 BAD_REQUEST (single error)" should {
      val httpResponse = HttpResponse(Status.BAD_REQUEST,
        Json.obj(
          "code" -> "VRN_INVALID",
          "reason" -> "Fail"
        ).toString()
      )
      val expected = Left(UnexpectedError(
        status = Status.BAD_REQUEST,
        message = Json.obj(
          "code" -> "VRN_INVALID",
          "reason" -> "Fail"
        ).toString()
      ))
      val result = PenaltiesReads.read("", "", httpResponse)

      "return a UnexpectedError" in {
        result shouldEqual expected
      }
    }

    "the http response status is 400 BAD_REQUEST (unknown API error json)" should {
      val httpResponse = HttpResponse(Status.BAD_REQUEST,
        Json.obj(
          "foo" -> "VRN_INVALID",
          "bar" -> "Fail"
        ).toString()
      )
      val expected = Left(UnexpectedError(
        status = Status.BAD_REQUEST,
        message = Json.obj(
          "foo" -> "VRN_INVALID",
          "bar" -> "Fail"
        ).toString()
      ))

      val result = PenaltiesReads.read("", "", httpResponse)

      "return a UnexpectedError" in {
        result shouldEqual expected
      }
    }

    "the HTTP response status is 5xx" should {
      val body: JsObject = Json.obj(
        "code" -> "GATEWAY_TIMEOUT",
        "message" -> "GATEWAY_TIMEOUT"
      )

      val httpResponse = HttpResponse(Status.GATEWAY_TIMEOUT, body.toString())
      val expected = Left(UnexpectedError(Status.GATEWAY_TIMEOUT, httpResponse.body))
      val result = PenaltiesReads.read("", "", httpResponse)

      "return a UnexpectedError" in {
        result shouldBe expected
      }
    }

    "the HTTP response status is 204" should {
      val httpResponse = HttpResponse(Status.NO_CONTENT, "")
      val expected = Right(PenaltiesSummary.empty)
      val result = PenaltiesReads.read("", "", httpResponse)

      "return an empty model" in {
        result shouldBe expected
      }
    }

    "the HTTP response status isn't handled" should {
      val body: JsObject = Json.obj(
        "code" -> "Conflict",
        "message" -> "CONFLICT"
      )

      val httpResponse = HttpResponse(Status.CONFLICT, body.toString())
      val expected = Left(UnexpectedError(Status.CONFLICT, httpResponse.body))
      val result = PenaltiesReads.read("", "", httpResponse)

      "return an UnexpectedError" in {
        result shouldBe expected
      }
    }
  }
}
