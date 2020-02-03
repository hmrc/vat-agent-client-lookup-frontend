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

package controllers.agent

import java.time.LocalDate
import java.util.concurrent.TimeUnit

import akka.util.Timeout
import assets.BaseTestConstants
import assets.CustomerDetailsTestConstants._
import assets.messages.WhatToDoMessages._
import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.services.{MockCustomerDetailsService, MockDateService}
import models.errors.UnexpectedError
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.redirectLocation
import play.mvc.Http.Status._
import views.html.agent.WhatToDoView

import scala.concurrent.Future

class WhatToDoControllerSpec extends ControllerBaseSpec with MockCustomerDetailsService with MockDateService {

  trait Test {
    lazy val controller =
      new WhatToDoController(
        mockAuthAsAgentWithClient,
        mockErrorHandler,
        mockCustomerDetailsService,
        mockDateService,
        mcc,
        inject[WhatToDoView],
        mockConfig,
        ec)
    implicit val timeout: Timeout = Timeout.apply(60, TimeUnit.SECONDS)
    val fakeRequestWithEmailPref: FakeRequest[AnyContentAsEmpty.type] = fakeRequestWithVrnAndRedirectUrl.withSession(
      SessionKeys.preference -> "true"
    )
  }

  "WhatToDoController.show" when {

    "useAgentHubFeature is disabled" should {

      "render the page" when {

        "user is an agent" in new Test {

          mockAgentAuthorised()
          mockCustomerDetailsSuccess(customerDetailsFnameOnly)

          val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

          status(result) shouldBe OK
        }
      }

      "render the error page" when {

        "an error is returned in customer details" in new Test {

          mockAgentAuthorised()
          mockCustomerDetailsError(BaseTestConstants.unexpectedError)

          val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

          status(result) shouldBe INTERNAL_SERVER_ERROR
          Jsoup.parse(bodyOf(result)).title() shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
        }
      }
    }

    "useAgentHubFeature is enabled" should {

      "redirect to Agent Hub controller route" in new Test {

        val result: Future[Result] = {
          mockConfig.features.useAgentHubPageFeature(true)

          mockAgentAuthorised()
          mockCustomerDetailsSuccess(customerDetailsFnameOnly)

          controller.show()(fakeRequestWithVrnAndRedirectUrl)
        }

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show().url)
      }
    }
  }

  "WhatToDoController.submit" should {

    setupMockDateService(LocalDate.parse(mockConfig.staticDateValue))

    "render the page" when {

      "submit return is selected" in new Test {

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl
          .withFormUrlEncodedBody("option" -> "submit-return")
        )

        redirectLocation(result) shouldBe Some(mockConfig.returnDeadlinesUrl)
      }

      "view return is selected" in new Test {

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl
          .withFormUrlEncodedBody("option" -> "view-return")
        )

        redirectLocation(result) shouldBe Some(mockConfig.submittedReturnsUrl)
      }

      "change details is selected with email pref yes and verified email in session" in new Test {

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl.withSession(
          SessionKeys.preference -> "yes", SessionKeys.verifiedAgentEmail -> "verified@email.com")
          .withFormUrlEncodedBody("option" -> "change-details")
        )

        redirectLocation(result) shouldBe Some(mockConfig.manageVatCustomerDetailsUrl)
      }

      "change details is selected with email pref yes and no verified email in session" in new Test {

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl.withSession(
          SessionKeys.preference -> "yes")
          .withFormUrlEncodedBody("option" -> "change-details")
        )

        redirectLocation(result) shouldBe Some("/vat-through-software/representative/email-notification")
      }

      "change details is selected with email pref no" in new Test {

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl.withSession(
          SessionKeys.preference -> "no")
          .withFormUrlEncodedBody("option" -> "change-details")
        )

        redirectLocation(result) shouldBe Some(mockConfig.manageVatCustomerDetailsUrl)
      }

      "view certificate is selected" in new Test {

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl
          .withFormUrlEncodedBody("option" -> "view-certificate")
        )

        redirectLocation(result) shouldBe Some(mockConfig.vatCertificateUrl)
      }
    }
    
    "render the page with an error" when {

      "the form submitted is incorrect" in new Test {

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithMtdVatAgentData)
        val parsedBody: Document = Jsoup.parse(bodyOf(result))

        status(result) shouldBe BAD_REQUEST
        messages(parsedBody.select("#option-error-summary").text) shouldBe error
      }

      "the form submitted is incorrect, there's no session data and customerDetailService call is successful" in new Test {

        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl)
        val parsedBody: Document = Jsoup.parse(bodyOf(result))

        status(result) shouldBe BAD_REQUEST
        messages(parsedBody.select("#option-error-summary").text) shouldBe error
      }
    }

    "redirect to the capture preferences page" when {

      "the email preference is not present in the session" in new Test {
        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl
          .withFormUrlEncodedBody("option" -> "change-details")
        )

        redirectLocation(result) shouldBe Some("/vat-through-software/representative/email-notification")
      }
    }

    "return an ISE" when {

      "an incorrect form is submitted with no session data and customerDetailsService returns an error" in new Test{

        mockAgentAuthorised()
        mockCustomerDetailsError(UnexpectedError(INTERNAL_SERVER_ERROR, "It's ok, this is just a test"))

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl
        )
        val parsedBody: Document = Jsoup.parse(bodyOf(result))

        status(result) shouldBe INTERNAL_SERVER_ERROR
        parsedBody.title shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }
  }
}
