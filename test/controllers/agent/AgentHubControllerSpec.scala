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

import java.time.LocalDate
import assets.BaseTestConstants
import assets.CustomerDetailsTestConstants._
import assets.FinancialDataConstants._
import controllers.ControllerBaseSpec
import mocks.services._
import org.jsoup.Jsoup
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import play.mvc.Http.Status._
import views.html.agent.AgentHubView
import models.HubViewModel

import scala.concurrent.Future

class AgentHubControllerSpec extends ControllerBaseSpec
                              with MockCustomerDetailsService
                              with MockDateService
                              with MockFinancialDataService {

  lazy val controller = new AgentHubController(
    mockAuthAsAgentWithClient,
    mockErrorHandler,
    mockCustomerDetailsService,
    mockDateService,
    mockFinancialDataService,
    mcc,
    inject[AgentHubView]
  )

  val staticDate: LocalDate = LocalDate.parse("2018-05-01")

  "AgentHubController.show()" when {

    "the customer is a missing trader" when {

      "they do not have a pending PPOB" should {

        "redirect the customer to manage-vat" in {
          mockAgentAuthorised()
          mockCustomerDetailsSuccess(customerDetailsAllInfo)
          setupMockDateService(staticDate)
          mockPaymentResponse(paymentResponse)

          val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

          status(result) shouldBe SEE_OTHER
        }
      }

      "they have a pending PPOB" should {

        "render the AgentHubPage" in {
          mockAgentAuthorised()
          mockCustomerDetailsSuccess(customerDetailsAllPending.copy(missingTrader = true))
          setupMockDateService(staticDate)
          mockPaymentResponse(paymentResponse)

          val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

          status(result) shouldBe OK
          messages(Jsoup.parse(contentAsString(result)).select("h1").text) shouldBe "Your client’s VAT details"
        }
      }
    }

    "the customer isn't a missing trader" should {

      "render the AgentHubPage" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        messages(Jsoup.parse(contentAsString(result)).select("h1").text) shouldBe "Your client’s VAT details"
      }
    }

    "the customerDetails call fails" should {

      "return an error" in {
        mockAgentAuthorised()
        mockCustomerDetailsError(BaseTestConstants.unexpectedError)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        Jsoup.parse(contentAsString(result)).title() shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }

    "the customer is a hybrid user" should {

      "return OK but not make a payment API call" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsHybrid)
        setupMockDateService(staticDate)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)
        status(result) shouldBe OK
      }
    }
  }

  "AgentHubController.constructViewModel()" should {

    "build the correct view model" when {

      "the user has 1 payment and it is overdue" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          showBlueBox = true,
          Some(LocalDate.parse("2018-01-01")),
          isOverdue = true,
          payments = 1
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentOverdue)(userWithBlueBox)
        result shouldBe expected
      }

      "the user has several payments and 1 is overdue" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          showBlueBox = true,
          Some(LocalDate.parse("2018-01-01")),
          isOverdue = false,
          payments = 2
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsOverdue)(userWithBlueBox)
        result shouldBe expected
      }

      "the user has several payments and none are overdue" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          showBlueBox = true,
          Some(LocalDate.parse("2020-01-01")),
          isOverdue = false,
          payments = 2
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsNotOverdue)(userWithBlueBox)
        result shouldBe expected
      }
    }
  }
}
