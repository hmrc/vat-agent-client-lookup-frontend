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
import assets.FinancialDataConstants._
import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.services.{MockCustomerDetailsService, MockDateService, MockFinancialDataService}
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.agent.DirectDebitInterruptView

import java.time.LocalDate
import scala.concurrent.Future

class DDInterruptControllerSpec extends ControllerBaseSpec with MockDateService with
  MockCustomerDetailsService with MockFinancialDataService {

  lazy val controller = new DDInterruptController(
    mcc,
    mockAuthAsAgentWithClient,
    inject[DirectDebitInterruptView],
    mockDateService,
    mockCustomerDetailsService,
    mockFinancialDataService,
  )
  lazy val ddSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    fakeRequestWithMtdVatAgentData.withSession(SessionKeys.viewedDDInterrupt -> "true")

  val staticDate: LocalDate = LocalDate.parse("2018-05-01")

  def redirectAssertions(result: Future[Result]): Unit = {
    "return 303" in {
      status(result) shouldBe Status.SEE_OTHER
    }

    "redirect to the ConfirmClientVrnController" in {
      redirectLocation(result) shouldBe Some(controllers.agent.routes.ConfirmClientVrnController.redirect().url)
    }

    "add the DD session key to the session" in {
      session(result).get(SessionKeys.viewedDDInterrupt) shouldBe Some("true")
    }
  }

  "The .show action" when {

    "the DD feature switch is on" when {

      "the client was migrated within 4 months" when {

        "the client does not have a direct debit mandate" should {

          lazy val result = {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsIndividual)
            setupMockDateService(staticDate)
            mockDirectDebitResponse(ddNoMandateFound)
            controller.show(fakeRequestWithVrnAndRedirectUrl)
          }

          "return 200" in {
            status(result) shouldBe Status.OK
          }
        }

        "the client has a direct debit mandate" should {
          lazy val result = {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsIndividual)
            setupMockDateService(staticDate)
            mockDirectDebitResponse(ddMandateFound)
            controller.show(fakeRequestWithVrnAndRedirectUrl)
          }
          redirectAssertions(result)
        }

        "the DD call fails" should {
          lazy val result = {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsIndividual)
            setupMockDateService(staticDate)
            mockDirectDebitResponse(ddFailureResponse)
            controller.show(fakeRequestWithVrnAndRedirectUrl)
          }
          redirectAssertions(result)
        }
      }

      "the client was migrated over 4 months ago" should {
        lazy val result = {
          mockAgentAuthorised()
          mockCustomerDetailsSuccess(customerDetailsIndividual.copy(customerMigratedToETMPDate = Some("2017-01-01")))
          setupMockDateService(staticDate)
          controller.show(fakeRequestWithVrnAndRedirectUrl)
        }
        redirectAssertions(result)
      }

      "the customer info call fails" should {
        lazy val result = {
          mockAgentAuthorised()
          mockCustomerDetailsError(BaseTestConstants.unexpectedError)
          controller.show(fakeRequestWithVrnAndRedirectUrl)
        }
        redirectAssertions(result)
      }
    }

    "the DD feature switch is off" should {
      lazy val result = {
        mockAgentAuthorised()
        mockConfig.features.directDebitInterruptFeature(false)
        controller.show(fakeRequestWithVrnAndRedirectUrl)
      }
      redirectAssertions(result)
    }

    "the user is unauthorised" should {
      lazy val result = {
        mockUnauthorised()
        controller.show(FakeRequest())
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the Select Client VRN controller (no client VRN in session)" in {
        redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show().url)
      }
    }
  }

  "The .submit action" when {

    "the user is authorised" when {

      "the DD interrupt feature switch is on" when {

        "the user has the DD interrupt session key" should {

          lazy val result = {
            mockAgentAuthorised()
            controller.submit(ddSessionRequest)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Agent Hub controller" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show().url)
          }
        }

        "the user does not have the DD interrupt session key" when {

          "the form binds successfully (the user ticked the checkbox)" should {

            lazy val result = {
              mockAgentAuthorised()
              controller.submit(fakeRequestWithMtdVatAgentData.withFormUrlEncodedBody(("checkbox", "true")))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the Agent Hub controller" in {
              redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show().url)
            }

            "add a value to the session to confirm the user has seen and accepted the terms of the DD interrupt" in {
              session(result).get(SessionKeys.viewedDDInterrupt) shouldBe Some("blueBox")
            }
          }

          "there is a form error (the user did not tick the checkbox)" should {

            lazy val result = {
              mockAgentAuthorised()
              controller.submit(fakeRequestWithMtdVatAgentData.withFormUrlEncodedBody(("checkbox", "")))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }
          }
        }
      }

      "the DD interrupt feature switch is off" when {

        "the user has the DD interrupt session key" should {

          lazy val result = {
            mockConfig.features.directDebitInterruptFeature(false)
            mockAgentAuthorised()
            controller.submit(ddSessionRequest)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Agent Hub controller" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show().url)
          }
        }

        "the user does not have the DD interrupt session key" should {

          lazy val result = {
            mockConfig.features.directDebitInterruptFeature(false)
            mockAgentAuthorised()
            controller.submit(fakeRequestWithMtdVatAgentData)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Agent Hub controller" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show().url)
          }
        }
      }
    }

    "the user is unauthorised" should {

      lazy val result = {
        mockUnauthorised()
        controller.submit(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the Select Client VRN controller (no client VRN in session)" in {
        redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show().url)
      }
    }
  }

  "The .migratedWithin4M function" when {

    "the migration date is more recent than 4 months" should {

      "return true" in {
        setupMockDateService(staticDate)
        controller.migratedWithin4M(customerDetailsIndividual) shouldBe true
      }
    }

    "the migration date is further back than 4 months" should {

      "return false" in {
        setupMockDateService(staticDate)
        controller.migratedWithin4M(
          customerDetailsIndividual.copy(customerMigratedToETMPDate = Some("2017-01-01"))
        ) shouldBe false
      }
    }

    "there is no migration date provided" should {

      "return false" in {
        controller.migratedWithin4M(customerDetailsNoInfo) shouldBe false
      }
    }
  }
}
