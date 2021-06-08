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

package controllers.agent

import assets.BaseTestConstants
import assets.CustomerDetailsTestConstants._
import assets.DirectDebitConstants.{ddFailureResponse, ddMandateFound, ddNoMandateFound}
import common.SessionKeys.viewedDDInterrupt
import controllers.ControllerBaseSpec
import mocks.services._
import org.jsoup.Jsoup
import play.api.mvc.Result
import play.mvc.Http.Status._
import views.html.agent.{AgentHubView, DirectDebitInterruptView}

import scala.concurrent.Future

class AgentHubControllerSpec extends ControllerBaseSpec
                              with MockCustomerDetailsService
                              with MockDateService
                              with MockDirectDebitService {

  lazy val controller = new AgentHubController(
    mockAuthAsAgentWithClient,
    mockErrorHandler,
    mockCustomerDetailsService,
    mockDateService,
    mockDirectDebitService,
    mcc,
    inject[AgentHubView],
    inject[DirectDebitInterruptView],
    mockConfig,
    ec
  )

  "AgentHubController.show()" when {

    "the DD interrupt feature switch is off" when {

      "the customer is a missing trader" when {

        "they do not have a pending PPOB" should {

          "redirect the customer to manage-vat" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsAllInfo)
            val result: Future[Result] = {
              mockConfig.features.directDebitInterruptFeature(false)
              controller.show()(fakeRequestWithVrnAndRedirectUrl)
            }

            status(result) shouldBe SEE_OTHER
          }
        }

        "they have a pending PPOB" should {

          "render the AgentHubPage" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsAllPending.copy(missingTrader = true))

            val result: Future[Result] = {
              mockConfig.features.directDebitInterruptFeature(false)
              controller.show()(fakeRequestWithVrnAndRedirectUrl)
            }

            status(result) shouldBe OK
            messages(Jsoup.parse(bodyOf(result)).select("h1").text) shouldBe "Your client’s VAT details"
          }
        }
      }

      "the customer isn't a missing trader" should {

        "render the AgentHubPage" in {
          mockAgentAuthorised()
          mockCustomerDetailsSuccess(customerDetailsFnameOnly)

          val result: Future[Result] = {
            mockConfig.features.directDebitInterruptFeature(false)
            controller.show()(fakeRequestWithVrnAndRedirectUrl)
          }

          status(result) shouldBe OK
          messages(Jsoup.parse(bodyOf(result)).select("h1").text) shouldBe "Your client’s VAT details"
        }
      }
    }

    "the customerDetails call fails" should {

      "return an error" in {
        mockAgentAuthorised()
        mockCustomerDetailsError(BaseTestConstants.unexpectedError)

        val result: Future[Result] = {
          mockConfig.features.directDebitInterruptFeature(false)
          controller.show()(fakeRequestWithVrnAndRedirectUrl)
        }

        status(result) shouldBe INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title() shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }
  }

  "the DD interrupt feature is on" when {

    "the direct debit service returns false" should {

      lazy val result: Future[Result] = {
        mockConfig.features.directDebitInterruptFeature(true)
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsIndividual)
        mockDirectDebitResponse(ddNoMandateFound)
        controller.show()(fakeRequestWithVrnAndRedirectUrl)
      }

      "return 200" in {
        status(result) shouldBe OK
      }

      "render the DD interrupt page" in {
        messages(Jsoup.parse(bodyOf(result)).select("h1").text) shouldBe "Your client needs to set up a new Direct Debit"
      }

    }

    "the direct debit service returns true" should {

      lazy val result: Future[Result] = {
        mockConfig.features.directDebitInterruptFeature(true)
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsIndividual)
        mockDirectDebitResponse(ddMandateFound)
        controller.show()(fakeRequestWithVrnAndRedirectUrl)
      }

      "return 200" in {
        status(result) shouldBe OK
      }

      "render the agent hub page" in {
        messages(Jsoup.parse(bodyOf(result)).select("h1").text) shouldBe "Your client’s VAT details"
      }
    }

    "the customer has already viewed the DD interrupt page" should {

      lazy val result: Future[Result] = {
        mockConfig.features.directDebitInterruptFeature(true)
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsIndividual)
        controller.show()(fakeRequestWithVrnAndRedirectUrl.withSession(viewedDDInterrupt -> "true"))
      }

      "return 200" in {
        status(result) shouldBe OK
      }

      "render the agent hub page" in {
         messages(Jsoup.parse(bodyOf(result)).select("h1").text) shouldBe "Your client’s VAT details"
      }

    }

    "the DD service returns an error" should {

      lazy val result: Future[Result] = {
        mockConfig.features.directDebitInterruptFeature(true)
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsIndividual)
        mockDirectDebitResponse(ddFailureResponse)
        controller.show()(fakeRequestWithVrnAndRedirectUrl)
      }

      "return 200" in {
        status(result) shouldBe OK
      }

      "render the agent hub page" in {
        messages(Jsoup.parse(bodyOf(result)).select("h1").text) shouldBe "Your client’s VAT details"
      }
    }
  }
}
