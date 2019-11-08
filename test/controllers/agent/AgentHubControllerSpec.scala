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

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import assets.BaseTestConstants
import assets.CustomerDetailsTestConstants.{customerDetailsFnameOnly, firstName}
import assets.messages.WhatToDoMessages.title
import controllers.ControllerBaseSpec
import mocks.services.MockCustomerDetailsService
import org.jsoup.Jsoup
import play.api.mvc.Result
import play.mvc.Http.Status._

import scala.concurrent.Future

class AgentHubControllerSpec extends ControllerBaseSpec with MockCustomerDetailsService {

  trait Test {
    lazy val controller = new AgentHubController(messagesApi, mockAuthAsAgentWithClient, mockErrorHandler, mockCustomerDetailsService, mockConfig)
    implicit val timeout: Timeout = Timeout.apply(60, TimeUnit.SECONDS)
  }

  "AgentHubController.show()" when {
    "the useAgentHub feature switch is enabled" when {

      "the customerDetailsService returns the customer details" should {

        "render the AgentHubPage" in new Test {

          mockAgentAuthorised()
          mockCustomerDetailsSuccess(customerDetailsFnameOnly)

          val result: Future[Result] = {
            mockConfig.features.useAgentHubPageFeature(true)
            controller.show()(fakeRequestWithVrnAndRedirectUrl)
          }

          status(result) shouldBe OK //TODO - add test to check page title when AgentHubPage has been implemented
        }
      }

      "the customerDetails call fails" should {

        "return an error" in new Test {

          mockAgentAuthorised()
          mockCustomerDetailsError(BaseTestConstants.unexpectedError)

          val result: Future[Result] = {
            mockConfig.features.useAgentHubPageFeature(true)
            controller.show()(fakeRequestWithVrnAndRedirectUrl)
          }

          status(result) shouldBe INTERNAL_SERVER_ERROR
          Jsoup.parse(bodyOf(result)).title() shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
        }
      }
    }

    "the useAgentHub feature switch is disabled" should {

      "redirect to the WhatToDo page" in new Test {
        mockConfig.features.useAgentHubPageFeature(false)

        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe SEE_OTHER
      }
    }


  }

}
