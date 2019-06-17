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
import assets.BaseTestConstants.{arn, vrn}
import assets.CustomerDetailsTestConstants._
import assets.messages.{ConfirmClientVrnPageMessages => Messages}
import audit.mocks.MockAuditingService
import audit.models.{AuthenticateAgentAuditModel, GetClientBusinessNameAuditModel}
import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.services.MockCustomerDetailsService
import models.errors.{Migration, NotSignedUp}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.verify
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class ConfirmClientVrnControllerSpec extends ControllerBaseSpec with MockCustomerDetailsService with MockAuditingService {

  object TestConfirmClientVrnController extends ConfirmClientVrnController(
    messagesApi,
    mockAuthAsAgentWithClient,
    mockCustomerDetailsService,
    serviceErrorHandler,
    mockAuditingService,
    mockConfig
  )

  override def beforeEach(): Unit = {
    mockConfig.features.whereToGoFeature(false)
  }

  "Calling the .show action" when {

    "the user is an Agent" when {

      "the Agent is authorised and signed up to HMRC-AS-AGENT" when {

        "a Client's VRN is held in Session" when {

          "details are successfully retrieved" should {

            lazy val result = TestConfirmClientVrnController.show(fakeRequestWithVrnAndRedirectUrl)
            lazy val document = Jsoup.parse(bodyOf(result))

            "return 200" in {
              mockAgentAuthorised()
              mockCustomerDetailsSuccess(customerDetailsOrganisation)
              status(result) shouldBe Status.OK

              verify(mockAuditingService)
                .extendedAudit(
                  ArgumentMatchers.eq(AuthenticateAgentAuditModel(arn, vrn, isAuthorisedForClient = true)),
                  ArgumentMatchers.eq[Option[String]](Some(controllers.agent.routes.ConfirmClientVrnController.show().url))
                )(
                  ArgumentMatchers.any[HeaderCarrier],
                  ArgumentMatchers.any[ExecutionContext]
                )

              verify(mockAuditingService)
                .extendedAudit(
                  ArgumentMatchers.eq(GetClientBusinessNameAuditModel(arn, vrn, customerDetailsOrganisation.clientName)),
                  ArgumentMatchers.eq[Option[String]](Some(controllers.agent.routes.ConfirmClientVrnController.show().url))
                )(
                  ArgumentMatchers.any[HeaderCarrier],
                  ArgumentMatchers.any[ExecutionContext]
                )
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "render the Confirm Client Vrn Page" in {
              document.select("h1").text shouldBe Messages.heading
            }
          }

          "a data migration error is retrieved" should {

            lazy val result = TestConfirmClientVrnController.show(fakeRequestWithVrnAndRedirectUrl)

            "return 412" in {
              mockAgentAuthorised()
              mockCustomerDetailsError(Migration)
              status(result) shouldBe Status.PRECONDITION_FAILED
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "return the migration error page" in {
              lazy val document = Jsoup.parse(bodyOf(result))
              document.select("h1").text shouldBe "You cannot make changes for that client’s business right now"
            }
          }

          "a business has not been signed up to MTD error is retrieved" should {

            lazy val result = TestConfirmClientVrnController.show(fakeRequestWithVrnAndRedirectUrl)

            "return 404" in {
              mockAgentAuthorised()
              mockCustomerDetailsError(NotSignedUp)
              status(result) shouldBe Status.NOT_FOUND
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "return the not signed up error page" in {
              lazy val document = Jsoup.parse(bodyOf(result))
              document.select("h1").text shouldBe "The business has not signed up to Making Tax Digital for VAT"
            }
          }

          "no details are retrieved" should {

            lazy val result = TestConfirmClientVrnController.show(fakeRequestWithVrnAndRedirectUrl)

            "return 500" in {
              mockAgentAuthorised()
              mockCustomerDetailsError(BaseTestConstants.unexpectedError)
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }
          }
        }

        "there is a redirect URL in session but no client VRN is found" should {

          lazy val result = TestConfirmClientVrnController.show(request.withSession(common.SessionKeys.redirectUrl -> "/homepage"))
          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to Select Client VRN" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show().url)
          }
        }
      }
    }

    "the user is not authenticated" should {

      "return 401 (Unauthorised)" in {
        mockMissingBearerToken()
        val result = TestConfirmClientVrnController.show(fakeRequestWithVrnAndRedirectUrl)
        status(result) shouldBe Status.UNAUTHORIZED
      }
    }
  }

  "Calling the .changeClient action" when {

    "the user is an Agent" when {

      "the Agent is authorised and signed up to HMRC-AS-AGENT" when {

        "a redirect URL and client's VRN are held in session" should {

          lazy val result = TestConfirmClientVrnController.changeClient(fakeRequestWithVrnAndRedirectUrl)

          "return status redirect SEE_OTHER (303)" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Select Your Client show action with the redirect URL from session" in {
            redirectLocation(result) shouldBe
              Some(controllers.agent.routes.SelectClientVrnController.show("/homepage").url)
          }
        }

        "a client's VRN is held in session, but no redirect URL" when {

          "whatToDo feature switch is on" should {

            lazy val result = {
              mockConfig.features.whereToGoFeature(true)
              TestConfirmClientVrnController.changeClient(request.withSession(
                SessionKeys.clientVRN -> vrn
              ))
            }

            "return status redirect SEE_OTHER (303)" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the Select Your Client show action with the no redirect URL" in {
              redirectLocation(result) shouldBe
                Some(controllers.agent.routes.SelectClientVrnController.show().url)
            }
          }

          "whatToDo feature switch is off" should {
            lazy val result = {
              mockConfig.features.whereToGoFeature(false)
              TestConfirmClientVrnController.changeClient(request.withSession(
                SessionKeys.clientVRN -> vrn
              ))
            }

            "return status redirect SEE_OTHER (303)" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the Select Your Client show action with the default redirect URL (ChoC overview)" in {
              redirectLocation(result) shouldBe
                Some(controllers.agent.routes.SelectClientVrnController.show(mockConfig.manageVatCustomerDetailsUrl).url)
            }
          }
        }
      }
    }

    "the user is not authenticated" should {

      "return 401 (Unauthorised)" in {
        mockMissingBearerToken()
        val result = TestConfirmClientVrnController.changeClient(fakeRequestWithVrnAndRedirectUrl)
        status(result) shouldBe Status.UNAUTHORIZED
      }
    }
  }

  "Calling the .redirect action" when {

    "the user is an Agent" when {

      "whatToDo feature is on" when {

        "redirect URL is in session" should {

          "notification preference is in session" should {

            lazy val result = {
              mockConfig.features.whereToGoFeature(true)
              TestConfirmClientVrnController.redirect(FakeRequest().withSession(
                SessionKeys.clientVRN -> vrn,
                SessionKeys.redirectUrl -> "/homepage",
                SessionKeys.preference -> "no"
              ))
            }

            "return status SEE_OTHER (303)" in {
              mockAgentAuthorised()
              mockCustomerDetailsSuccess(customerDetailsOrganisation)
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the redirect URL in session" in {
              redirectLocation(result) shouldBe Some("/homepage")
            }
          }

          "notification preference is not in session" should {

            lazy val result = {
              mockConfig.features.whereToGoFeature(true)
              TestConfirmClientVrnController.redirect(FakeRequest().withSession(
                SessionKeys.clientVRN -> vrn,
                SessionKeys.redirectUrl -> "/homepage"
              ))
            }

            "return status SEE_OTHER (303)" in {
              mockAgentAuthorised()
              mockCustomerDetailsSuccess(customerDetailsOrganisation)
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to CapturePreference controller" in {
              redirectLocation(result) shouldBe Some(controllers.agent.routes.CapturePreferenceController.show().url)
            }
          }
        }

        "redirect URL is not in session" should {

          lazy val result = {
            mockConfig.features.whereToGoFeature(true)
            TestConfirmClientVrnController.redirect(FakeRequest().withSession(SessionKeys.clientVRN -> vrn))
          }

          "return status SEE_OTHER (303)" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsOrganisation)
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to WhatToDo controller" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.WhatToDoController.show().url)
          }
        }
      }

      "whatToDo feature is off" when {

        "a clients VRN is held in session and a redirect URL is found" should {

          lazy val result = TestConfirmClientVrnController.redirect(fakeRequestWithVrnAndRedirectUrl)

          "return status SEE_OTHER (303)" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsOrganisation)
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Select Your Client show action" in {
            redirectLocation(result) shouldBe Some("/homepage")
          }
        }

        "a client's VRN is held in session but no redirect URL is found" should {

          lazy val result = TestConfirmClientVrnController.redirect(request.withSession(common.SessionKeys.clientVRN -> vrn))

          "return status INTERNAL_SERVER_ERROR (500)" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsOrganisation)
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }
        }
      }
    }
  }
}
