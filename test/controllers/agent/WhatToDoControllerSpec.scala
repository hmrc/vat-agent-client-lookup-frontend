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
import assets.CustomerDetailsTestConstants._
import assets.messages.WhatToDoMessages._
import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.services.MockCustomerDetailsService
import models.errors.UnexpectedError
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.redirectLocation
import play.mvc.Http.Status._

import scala.concurrent.Future

class WhatToDoControllerSpec extends ControllerBaseSpec with MockCustomerDetailsService{

  trait Test {
    lazy val controller = new WhatToDoController(messagesApi, mockAuthAsAgentWithClient, mockErrorHandler, mockCustomerDetailsService, mockConfig)
    implicit val timeout: Timeout = Timeout.apply(60, TimeUnit.SECONDS)
    val fakeRequestWithEmailPref: FakeRequest[AnyContentAsEmpty.type] = fakeRequestWithVrnAndRedirectUrl.withSession(
      SessionKeys.preference -> "true"
    )
  }

  "WhatToDoController.show" should {
    "render the page" when {
      "user is an agent" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe OK
        Jsoup.parse(bodyOf(result)).title() shouldBe (title(firstName) + " - Your client’s VAT details - GOV.UK")
      }
    }
    "render the error page" when {
      "an error is returned in customer details" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()
        mockCustomerDetailsError(BaseTestConstants.unexpectedError)

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title() shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }

      "the whereToGo feature is off" in new Test {
        mockConfig.features.whereToGoFeature(false)

        mockAgentAuthorised()

        val result: Future[Result] = controller.show()(fakeRequestWithVrnAndRedirectUrl)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title() shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }
  }

  "WhatToDoController.submit" should {
    "render the page" when {
      "option 1 is selected" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl
          .withFormUrlEncodedBody("option" -> "submit-return")
        )

        redirectLocation(result) shouldBe Some(mockConfig.returnDeadlinesUrl)
      }
      "option 2 is selected" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl
          .withFormUrlEncodedBody("option" -> "view-return")
        )

        redirectLocation(result) shouldBe Some(mockConfig.submittedReturnsUrl(1993))
      }
      "option 3 is selected with email preference in session" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl.withSession(SessionKeys.preference -> "yes")
          .withFormUrlEncodedBody("option" -> "change-details")
        )

        redirectLocation(result) shouldBe Some(mockConfig.manageVatCustomerDetailsUrl)
      }
      "option 4 is selected" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl
          .withFormUrlEncodedBody("option" -> "view-certificate")
        )

        redirectLocation(result) shouldBe Some(mockConfig.vatCertificateUrl)
      }
    }
    
    "render the page with an error" when {
      "the form submitted is incorrect" in new Test {
        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithMtdVatAgentData)
        val parsedBody: Document = Jsoup.parse(bodyOf(result))

        status(result) shouldBe BAD_REQUEST
        parsedBody.title() shouldBe "Error: " + (title("l'biz") + " - Your client’s VAT details - GOV.UK")

        parsedBody.body().toString should include(error)
      }

      "the feature switch is off" in new Test {
        mockConfig.features.whereToGoFeature(false)

        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl)
        val parsedBody: Document = Jsoup.parse(bodyOf(result))

        status(result) shouldBe INTERNAL_SERVER_ERROR
        parsedBody.title shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }

      "the form submitted is incorrect, there's no session data and customerDetailService call is successful" in new Test {

        mockConfig.features.whereToGoFeature(true)

        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsFnameOnly)

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl)
        val parsedBody: Document = Jsoup.parse(bodyOf(result))

        status(result) shouldBe BAD_REQUEST
        parsedBody.title() shouldBe "Error: " + (title(firstName) + " - Your client’s VAT details - GOV.UK")

        parsedBody.body().toString should include(error)
      }
    }
    "redirect to the capture preferences page" when {
      "the email preference is not present in the session" in new Test {
        mockConfig.features.whereToGoFeature(true)
        mockAgentAuthorised()

        val result: Future[Result] = controller.submit(fakeRequestWithVrnAndRedirectUrl
          .withFormUrlEncodedBody("option" -> "change-details")
        )

        redirectLocation(result) shouldBe Some("/vat-through-software/representative/email-notification")
      }
    }
    "return an ISE" when {

      "an incorrect form is submitted with no session data and customerDetailsService returns an error" in new Test{
        mockConfig.features.whereToGoFeature(true)

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
