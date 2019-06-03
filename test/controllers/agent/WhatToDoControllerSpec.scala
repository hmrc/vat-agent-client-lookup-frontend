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

package controllers.agent

import assets.BaseTestConstants
import assets.CustomerDetailsTestConstants.{customerDetailsFnameOnly, firstName}
import controllers.ControllerBaseSpec
import controllers.predicates.AuthoriseAsAgentOnly
import play.api.mvc._
import play.api.test.FakeRequest
import play.mvc.Http.Status._
import assets.messages.WhatToDoMessages._
import mocks.services.MockCustomerDetailsService
import models.Agent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.Future

class WhatToDoControllerSpec extends ControllerBaseSpec with MockCustomerDetailsService{

  trait Test {
    lazy val controller = new WhatToDoController(messagesApi, mockAuthAsAgentWithClient, mockErrorHandler, mockCustomerDetailsService, mockConfig)
  }

  ".show" should {
    "render the page" when {
      "user is an agent" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        Jsoup.parse(bodyOf(result)).title() shouldBe title(firstName)
      }
    }
    "render the error page" when {
      "an error is returned in customer details" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()
        mockCustomerDetailsError(BaseTestConstants.unexpectedError)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the whereToGo feature is off" in new Test {
        mockConfig.features.whereToGoFeature(false)

        mockAgentAuthorised()

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title() shouldBe "There is a problem with the service - VAT reporting through software - GOV.UK"
      }
    }
  }

  ".submit" should {
    "render the page" when {
      "option 1 is selected" in pending
      "option 2 is selected" in pending
      "option 3 is selected" in pending
      "option 4 is selected" in pending
    }
    "render the page with an error" when {
      "the form submitted is incorrect" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit("l'biz", true)(fakeRequestWithVrnAndRedirectUrl)
        val parsedBody: Document = Jsoup.parse(bodyOf(result))

        status(result) shouldBe BAD_REQUEST
        parsedBody.title() shouldBe "Error: " + title("l'biz")

        parsedBody.body().toString should include(error)
      }
    }
  }
}
