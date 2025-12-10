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

package controllers.agent

import assets.BaseTestConstants
import assets.BaseTestConstants.{arn, vrn}
import assets.CustomerDetailsTestConstants._
import assets.FinancialDataConstants._
import assets.PenaltiesConstants._
import audit.mocks.MockAuditingService
import audit.models.AgentOverviewPageViewAuditModel
import controllers.ControllerBaseSpec
import mocks.services._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import models.{CustomerDetails, HubViewModel, RequestItem, StandingRequest, StandingRequestDetail, User, VatDetailsDataModel}
import models.errors.UnexpectedError
import models.penalties.PenaltiesSummary
import org.jsoup.Jsoup
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, status}
import play.mvc.Http.Status._
import views.html.agent.AgentHubView

import java.time.LocalDate
import scala.concurrent.Future

class AgentHubControllerSpec extends ControllerBaseSpec
                              with MockCustomerDetailsService
                              with MockDateService
                              with MockFinancialDataService
                              with MockPenaltiesService
                              with MockAuditingService
                              with MockPaymentsOnAccountService
                              with MockPoaCheckService
                              with MockAnnualAccountingCheckService {

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockAAChangedOn(None)
    when(mockPaymentsOnAccountService.getPaymentsOnAccounts(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(None))
  }

  lazy val controller = new AgentHubController(
    mockAuthAsAgentWithClient,
    mockErrorHandler,
    mockCustomerDetailsService,
    mockDateService,
    mockFinancialDataService,
    mockPenaltiesService,
    mockAuditingService,
    mcc,
    inject[AgentHubView],
    mockPaymentsOnAccountService,
    mockPoaCheckService,
    mockAnnualAccountingCheckService
  )

  val staticDate: LocalDate = LocalDate.parse("2018-05-01")
  val staticDateForPoa: LocalDate = LocalDate.parse("2020-03-01")


  val modelStandingRequestScheduleValid: StandingRequest = StandingRequest(
    ("2024-07-15"), List(
      StandingRequestDetail(
        requestNumber = "20000037272",
        requestCategory = "3",
        createdOn = ("2023-11-30"),
        changedOn = Some("2025-02-20"),
        requestItems = List(
          RequestItem(
            period = "1",
            periodKey = "25A1",
            startDate = ("2025-04-01"),
            endDate = ("2025-06-30"),
            dueDate = ("2025-06-30"),
            amount = 22945.23,
            chargeReference = Some("XD006411191344"),
            postingDueDate = Some("2025-06-30")
          ),
          RequestItem(
            period = "2",
            periodKey = "24A2",
            startDate = ("2024-02-01"),
            endDate = ("2024-04-30"),
            dueDate = ("2024-04-30"),
            amount = 22945.23,
            chargeReference = Some("XD006411191345"),
            postingDueDate = Some("2024-04-30")
          )
        )
      ),
      StandingRequestDetail(
        requestNumber = "20000037273",
        requestCategory = "2",
        createdOn = ("2023-11-30"),
        changedOn = Some("2025-02-01"),
        requestItems = List(
          RequestItem(
            period = "1",
            periodKey = "25A1",
            startDate = ("2025-04-01"),
            endDate = ("2025-06-30"),
            dueDate = ("2025-06-30"),
            amount = 22945.23,
            chargeReference = Some("XD006411191344"),
            postingDueDate = Some("2025-06-30")
          ),
          RequestItem(
            period = "2",
            periodKey = "24A2",
            startDate = ("2024-02-01"),
            endDate = ("2024-04-30"),
            dueDate = ("2024-04-30"),
            amount = 22945.23,
            chargeReference = Some("XD006411191345"),
            postingDueDate = Some("2024-04-30")
          )
        )
      )
    )
  )
  val modelStandingRequestScheduleInValid: StandingRequest = StandingRequest(
    ("2024-07-15T09:30:47Z"), List(
      StandingRequestDetail(
        requestNumber = "20000037272",
        requestCategory = "2",
        createdOn = ("2023-11-30"),
        changedOn = None,
        requestItems = List(
          RequestItem(
            period = "1",
            periodKey = "25A1",
            startDate = ("2025-04-01"),
            endDate = ("2025-06-30"),
            dueDate = ("2025-06-30"),
            amount = 22945.23,
            chargeReference = None,
            postingDueDate = None
          )
        )
      )
    )
  )

  lazy val agentUser: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true, Some(arn))(fakeRequestWithVrnAndRedirectUrl)

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
          mockChangedOnDateWithInLatestVatPeriod(None)

          val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

          status(result) shouldBe OK
          Jsoup.parse(contentAsString(result)).select("h1").text shouldBe "Your client’s VAT details"
        }
      }
    }

    "the customer isn't a missing trader" should {

      def result: Future[Result] = {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)
        mockPenaltiesResponse(penaltiesSummaryNoPenaltiesResponse)
        mockChangedOnDateWithInLatestVatPeriod(None)

        controller.show()(fakeRequestWithVrnAndRedirectUrl)
      }

      "render the AgentHubPage" in {
        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("h1").text shouldBe "Your client’s VAT details"
      }

      "audit the correct data in the AgentOverviewPageView audit event" in {
        await(result)
        verifyExtendedAudit(
          AgentOverviewPageViewAuditModel(agentUser, VatDetailsDataModel(paymentResponse.toOption.get, isError = false)),
          Some(routes.AgentHubController.show.url)
        )
      }
    }

    "the customer has penalties" should {

      "render the AgentHubPage" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentResponse)
        mockPenaltiesResponse(penaltiesSummaryResponse)
        mockStandingRequest(None)
        mockChangedOnDateWithInLatestVatPeriod(None)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("#penalties-heading").text shouldBe "Penalties for late VAT Returns and payments"
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

      def result: Future[Result] = {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)
        setupMockDateService(staticDate)
        mockPaymentResponse(paymentOnAccountResponse)
        mockPenaltiesResponse(penaltiesSummaryNoPenaltiesResponse)

        controller.show()(fakeRequestWithVrnAndRedirectUrl)
      }

      "render the correct number of payments on the next payment tile" in {
        status(result) shouldBe OK
        Jsoup.parse(contentAsString(result)).select("#next-payment-paragraph").text should include("2")
      }

      "audit the correct data in the AgentOverviewPageView audit event" in {
        await(result)
        verifyExtendedAudit(
          AgentOverviewPageViewAuditModel(agentUser, VatDetailsDataModel(paymentsNotOverdue, isError = false)),
          Some(routes.AgentHubController.show.url)
        )
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

    "the customer has poa active and poa changed on" should {
      "have poa alert" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsWithValidPoaActive)
        setupMockDateService(staticDate)
        mockPenaltiesResponse(penaltiesSummaryNotFoundResponse)
        mockStandingRequest(Some(modelStandingRequestScheduleValid))
        mockChangedOnDateWithInLatestVatPeriod(Some(LocalDate.parse("2020-01-01")))
        mockConfig.features.poaActiveFeature(true)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)
        status(result) shouldBe OK
        val document = Jsoup.parse(contentAsString(result))

        document.select("h1").text shouldBe "Your client’s VAT details"
        document.select("#poa-alert-information").text shouldBe
          "The amounts due for your client’s payments on account were changed on 1 January 2020. Check their schedule for details."
        document.select("#poa-alert-information").select("a[href]").text() shouldBe "Check their schedule for details"
      }

      "have no poa alert" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsWithValidPoaActive)
        setupMockDateService(staticDate)
        mockPenaltiesResponse(penaltiesSummaryNotFoundResponse)
        mockStandingRequest(Some(modelStandingRequestScheduleValid))
        mockChangedOnDateWithInLatestVatPeriod(None)
        mockConfig.features.poaActiveFeature(false)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)
        status(result) shouldBe OK
        val document = Jsoup.parse(contentAsString(result))

        document.select("h1").text shouldBe "Your client’s VAT details"
        document.select("#poa-alert-information").text shouldBe ""
      }
    }
  }

  "the payments call fails" should {

    def result: Future[Result] = {
      mockAgentAuthorised()
      mockCustomerDetailsSuccess(customerDetailsFnameOnly)
      setupMockDateService(staticDate)
      mockPaymentResponse(Left(UnexpectedError(500, "")))

      controller.show()(fakeRequestWithVrnAndRedirectUrl)
    }

    "render the AgentHubPage" in {
      status(result) shouldBe OK
    }

    "audit the correct data in the AgentOverviewPageView audit event" in {
      await(result)
      verifyExtendedAudit(
        AgentOverviewPageViewAuditModel(agentUser, VatDetailsDataModel(Seq(), isError = true)),
        Some(routes.AgentHubController.show.url)
      )
    }
  }

  "AgentHubController.constructViewModel()" should {

    "build the correct view model" when {

      "the user has 1 payment and it is overdue" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)
        mockChangedOnDateWithInLatestVatPeriod(None)
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

        val result = controller.constructViewModel(customerDetailsAllInfo, onePaymentModelOverdue, None, None)(user)
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

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelOneOverdue, None, None)(user)
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

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, None, None)(user)
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

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoPayments, None, None)(user)
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

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, None, None)(userHasDDSetup)
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

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, None, None)(userHasDDNotSetup)
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

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue,
          Some(penaltiesSummaryAsModel), None)(user)
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
          penaltiesSummary = Some(PenaltiesSummary(0, 0, 0, 0, 0, hasAnyPenaltyData = false))
        )

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue,
          Some(penaltiesSummaryAsModelNoPenalties), None)(user)
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

        val result = controller.constructViewModel(customerDetailsAllInfo, paymentsModelNoneOverdue, None, None)(user)
        result shouldBe expected
      }

      "the user has pao active until and also the changed-on date" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)
        mockStandingRequest(Some(modelStandingRequestScheduleValid))
        mockChangedOnDateWithInLatestVatPeriod(Some(LocalDate.parse("2020-01-01")))
        mockConfig.features.poaActiveFeature(true)

        val expected = HubViewModel(
          customerDetailsWithPoa,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          Some(LocalDate.parse("2020-01-01")),
          isOverdue = false,
          isError = false,
          payments = 2,
          directDebitSetup = None,
          penaltiesSummary = Some(penaltiesSummaryAsModel),
          isPoaActiveForCustomer = true,
          poaChangedOn = Some(LocalDate.parse("2020-01-01"))
        )

        val result = controller.constructViewModel(customerDetailsWithPoa, paymentsModelNoneOverdue,
          Some(penaltiesSummaryAsModel), Some(modelStandingRequestScheduleValid))(user)
        result shouldBe expected
      }

      "the user has pao active until and but changed-on date condition failed" in {
        mockAgentAuthorised()
        setupMockDateService(staticDate)
        mockStandingRequest(Some(modelStandingRequestScheduleInValid))
        mockChangedOnDateWithInLatestVatPeriod(None)
        mockConfig.features.poaActiveFeature(true)

        val expected = HubViewModel(
          customerDetailsWithPoa,
          BaseTestConstants.vrn,
          LocalDate.parse("2018-05-01"),
          Some(LocalDate.parse("2020-01-01")),
          isOverdue = false,
          isError = false,
          payments = 2,
          directDebitSetup = None,
          penaltiesSummary = Some(penaltiesSummaryAsModel),
          isPoaActiveForCustomer = true,
          poaChangedOn = None
        )

        val result = controller.constructViewModel(customerDetailsWithPoa, paymentsModelNoneOverdue,
          Some(penaltiesSummaryAsModel), Some(modelStandingRequestScheduleInValid))(user)
        result shouldBe expected
      }

    }
  }


  def customerInfoWithDate(date: Option[String]): CustomerDetails =
  customerDetailsAllInfo.copy(poaActiveUntil = date)
  val fixedDate: LocalDate = LocalDate.parse("2018-05-01")
  "retrievePoaActiveForCustomer" should {
    "return true when poaActiveUntil is today" in {
      mockDateService.now()
      val result = controller.retrievePoaActiveForCustomer(Right(customerInfoWithDate(Some(fixedDate.toString))))
      result shouldBe true
    }

    "return true when poaActiveUntil is in the future" in {
      mockDateService.now()
      val futureDate = fixedDate.plusDays(5).toString
      val result = controller.retrievePoaActiveForCustomer(Right(customerInfoWithDate(Some(futureDate))))
      result shouldBe true
    }
    "return false when poaActiveUntil is in the past" in {
      mockDateService.now()
      val pastDate = fixedDate.minusDays(5).toString
      val result = controller.retrievePoaActiveForCustomer(Right(customerInfoWithDate(Some(pastDate))))
      result shouldBe false
    }
    "return false when poaActiveUntil is None" in {
      mockDateService.now()
      val result = controller.retrievePoaActiveForCustomer(Right(customerInfoWithDate(None)))
      result shouldBe false
    }
    "return false when accountDetails is an error" in {
      mockDateService.now()
      val result = controller.retrievePoaActiveForCustomer(Left(UnexpectedError(500, "Internal Server Error")))
      result shouldBe false
    }
  }

}
