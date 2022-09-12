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

package controllers.agent

import assets.BaseTestConstants
import assets.CustomerDetailsTestConstants._
import assets.FinancialDataConstants._
import assets.PenaltiesConstants._
import controllers.ControllerBaseSpec
import mocks.services._
import models.HubViewModel
import models.errors.UnexpectedError
import models.penalties.PenaltiesSummary
import org.jsoup.Jsoup
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import play.mvc.Http.Status._
import views.html.agent.AgentHubView
import java.time.LocalDate
import scala.concurrent.Future

class AgentHubControllerSpec extends ControllerBaseSpec
                              with MockCustomerDetailsService
                              with MockDateService
                              with MockFinancialDataService
                              with MockPenaltiesService {

  lazy val controller = new AgentHubController(
    mockAuthAsAgentWithClient,
    mockErrorHandler,
    mockCustomerDetailsService,
    mockDateService,
    mockFinancialDataService,
    mockPenaltiesService,
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
          mockPenaltiesResponse(penaltiesSummaryNoPenaltiesResponse)

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
          mockPenaltiesResponse(penaltiesSummaryNoPenaltiesResponse)

          val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

          status(result) shouldBe OK
          Jsoup.parse(contentAsString(result)).select("h1").text shouldBe "Your client’s VAT details"
        }
      }
    }

    "the customer isn't a missing trader" should {

      "render the AgentHubPage" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)
        mockPenaltiesResponse(penaltiesSummaryNoPenaltiesResponse)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("h1").text shouldBe "Your client’s VAT details"
      }
    }

    "the customer has penalties" should {

      "render the AgentHubPage" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)
        mockPenaltiesResponse(penaltiesSummaryResponse)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("#penalties-heading").text shouldBe "Penalties and appeals"
      }
    }

    "the customer has no penalties" should {

      "render the AgentHubPage" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)
        mockPenaltiesResponse(penaltiesSummaryNoPenaltiesResponse)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("h1").text shouldBe "Your client’s VAT details"
        Jsoup.parse(contentAsString(result)).select("#penalties-heading").text shouldBe ""
      }
    }

    "the customer has some payments, including 1 payment on account" should {

      "render the correct number of payments on the next payment tile" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentOnAccountResponse)
        mockPenaltiesResponse(penaltiesSummaryNoPenaltiesResponse)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("#next-payment-paragraph").text should include("2")
      }
    }

    "the penalties call returns a 404 status" should {

      "render the AgentHubPage" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)
        mockPenaltiesResponse(penaltiesSummaryNotFoundResponse)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("h1").text shouldBe "Your client’s VAT details"
        Jsoup.parse(contentAsString(result)).select("#penalties-heading").text shouldBe ""
      }
    }

    "the penalties call fails" should {

      "render the AgentHubPage" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)
        mockPenaltiesResponse(Left(UnexpectedError(500, "")))

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("h1").text shouldBe "Your client’s VAT details"
        Jsoup.parse(contentAsString(result)).select("#penalties-heading").text shouldBe ""
      }
    }

    "the customerDetails call fails" should {

      "return an error" in {
        mockAgentAuthorised()
        mockCustomerDetailsError(BaseTestConstants.unexpectedError)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)
        mockPenaltiesResponse(penaltiesSummaryNoPenaltiesResponse)

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
        mockPenaltiesResponse(penaltiesSummaryNotFoundResponse)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)
        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("h1").text shouldBe "Your client’s VAT details"
      }
    }
  }

  "the payments call fails" should {

    "render the AgentHubPage" in {
      mockAgentAuthorised()
      mockCustomerDetailsSuccess(customerDetailsFnameOnly)
      setupMockDateService(staticDate)
      mockPaymentResponse(Left(UnexpectedError(500, "")))

      val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

      status(result) shouldBe OK
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
          Some(LocalDate.parse("2018-01-01")),
          isOverdue = true,
          isError = false,
          payments = 1,
          directDebitSetup = None
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, onePaymentModelOverdue, None)(user)
        result shouldBe expected
      }

      "the user has several payments and 1 is overdue" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          Some(LocalDate.parse("2018-01-01")),
          isOverdue = false,
          isError = false,
          payments = 2,
          directDebitSetup = None
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelOneOverdue, None)(user)
        result shouldBe expected
      }

      "the user has several payments and none are overdue" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          Some(LocalDate.parse("2020-01-01")),
          isOverdue = false,
          isError = false,
          payments = 2,
          directDebitSetup = None
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, None)(user)
        result shouldBe expected
      }

      "the user has no payments" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          None,
          isOverdue = false,
          isError = false,
          payments = 0,
          directDebitSetup = None
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoPayments, None)(user)
        result shouldBe expected
      }

      "the user has a DD set up" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          Some(LocalDate.parse("2020-01-01")),
          isOverdue = false,
          isError = false,
          payments = 2,
          directDebitSetup = Some(true)
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, None)(userHasDDSetup)
        result shouldBe expected
      }

      "the user has no DD set up" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          Some(LocalDate.parse("2020-01-01")),
          isOverdue = false,
          isError = false,
          payments = 2,
          directDebitSetup = Some(false)
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, None)(userHasDDNotSetup)
        result shouldBe expected
      }

      "the user has penalties" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          Some(LocalDate.parse("2020-01-01")),
          isOverdue = false,
          isError = false,
          payments = 2,
          directDebitSetup = None,
          penaltiesSummary = Some(penaltiesSummaryAsModel)
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, Some(penaltiesSummaryAsModel))(user)
        result shouldBe expected
      }

      "the user has no penalties" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          Some(LocalDate.parse("2020-01-01")),
          isOverdue = false,
          isError = false,
          payments = 2,
          directDebitSetup = None,
          penaltiesSummary = Some(PenaltiesSummary(0, 0, 0, 0, 0, false))
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, Some(penaltiesSummaryAsModelNoPenalties))(user)
        result shouldBe expected
      }

      "the user has no penalties data" in {

        mockAgentAuthorised()
        setupMockDateService(staticDate)

        val expected = HubViewModel(
          customerDetailsAllInfo,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          Some(LocalDate.parse("2020-01-01")),
          isOverdue = false,
          isError = false,
          payments = 2,
          directDebitSetup = None,
          penaltiesSummary = None
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, None)(user)
        result shouldBe expected
      }
    }
  }
}
