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
import views.html.agent.ConfirmClientVrnView
import views.html.errors.{AccountMigrationView, NotSignedUpView}

import scala.concurrent.ExecutionContext

class ConfirmClientVrnControllerSpec extends ControllerBaseSpec with MockCustomerDetailsService with MockAuditingService {

  object TestConfirmClientVrnController extends ConfirmClientVrnController(
    mockAuthAsAgentWithClient,
    mockDDPredicate,
    mockCustomerDetailsService,
    serviceErrorHandler,
    mockAuditingService,
    mcc,
    inject[ConfirmClientVrnView],
    inject[AccountMigrationView],
    inject[NotSignedUpView]
  )

  "Calling the .show action" when {

    "the user is an Agent" when {

      "the Agent is authorised and signed up to HMRC-AS-AGENT" when {

        "a Client's VRN is held in Session" when {

          "details are successfully retrieved" when {

            "the agent cannot act on behalf of the client due to insolvency" should {

              lazy val result = {
                mockAgentAuthorised()
                mockCustomerDetailsSuccess(customerDetailsInsolvent)
                TestConfirmClientVrnController.show(fakeRequestWithVrnAndRedirectUrl)
              }

              "return status SEE_OTHER (303)" in {
                status(result) shouldBe Status.SEE_OTHER
              }

              "redirect to the agentUnauthorisedForClient page" in {
                redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentUnauthorisedForClientController.show().url)
              }
            }

            "the agent is permitted through the insolvency check" should {

              lazy val result = TestConfirmClientVrnController.show(fakeRequestWithVrnAndRedirectUrl)
              lazy val document = Jsoup.parse(contentAsString(result))

              "return 200" in {
                mockAgentAuthorised()
                mockCustomerDetailsSuccess(customerDetailsOrganisation)
                status(result) shouldBe Status.OK

                verify(mockAuditingService)
                  .extendedAudit(
                    ArgumentMatchers.eq(AuthenticateAgentAuditModel(arn, vrn, isAuthorisedForClient = true)),
                    ArgumentMatchers.eq[Option[String]](Some(controllers.agent.routes.ConfirmClientVrnController.show.url))
                  )(
                    ArgumentMatchers.any[HeaderCarrier],
                    ArgumentMatchers.any[ExecutionContext]
                  )

                verify(mockAuditingService)
                  .extendedAudit(
                    ArgumentMatchers.eq(GetClientBusinessNameAuditModel(arn, vrn, customerDetailsOrganisation.clientName)),
                    ArgumentMatchers.eq[Option[String]](Some(controllers.agent.routes.ConfirmClientVrnController.show.url))
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
                messages(document.select("h1").text) shouldBe Messages.heading
              }

              "add the client name to the session" in {
                session(result).get(SessionKeys.mtdVatAgentClientName) shouldBe Some(customerDetailsOrganisation.clientName)
              }
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
              lazy val document = Jsoup.parse(contentAsString(result))
              messages(document.select("h1").text) shouldBe "You cannot make changes for that client’s business right now"
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
              lazy val document = Jsoup.parse(contentAsString(result))
              messages(document.select("h1").text) shouldBe "The business has not signed up to Making Tax Digital for VAT"
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

          lazy val result = TestConfirmClientVrnController.changeClient(
            request.withSession(
              SessionKeys.clientVRN -> vrn,
              SessionKeys.redirectUrl -> "/homepage",
              SessionKeys.notificationsEmail -> "an.email@host.com"
            )
          )

          "return status redirect SEE_OTHER (303)" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Select Your Client show action with the redirect URL from session" in {
            redirectLocation(result) shouldBe
              Some(controllers.agent.routes.SelectClientVrnController.show("/homepage").url)
          }

          "remove the client VRN" in {
            session(result).get(SessionKeys.clientVRN) shouldBe None
          }

          "remove the client name" in {
            session(result).get(SessionKeys.mtdVatAgentClientName) shouldBe None
          }

          "retain the agent email" in {
            session(result).get(SessionKeys.notificationsEmail) shouldBe Some("an.email@host.com")
          }
        }

        "a client's VRN is held in session, but no redirect URL" should {

          lazy val result = {
            TestConfirmClientVrnController.changeClient(request.withSession(
              SessionKeys.clientVRN -> vrn,
              SessionKeys.notificationsEmail -> "an.email@host.com"
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

          "remove the client VRN" in {
            session(result).get(SessionKeys.clientVRN) shouldBe None
          }

          "retain the agent email" in {
            session(result).get(SessionKeys.notificationsEmail) shouldBe Some("an.email@host.com")
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

    "the agent is permitted through the insolvency check" when {

      "redirect URL to a change service (contains /vat-through-software/account) is in session" when {

        "notification preference of no is in session" should {

          lazy val result = {
            TestConfirmClientVrnController.redirect(FakeRequest().withSession(
              SessionKeys.clientVRN -> vrn,
              SessionKeys.viewedDDInterrupt -> "true",
              SessionKeys.redirectUrl -> "/vat-through-software/account/change-something-about-vat",
              SessionKeys.preference -> "no",
              SessionKeys.notificationsEmail -> "an.email@host.com"
            ))
          }

          "return status SEE_OTHER (303)" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsOrganisation)
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to manage vat frontend" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatCustomerDetailsUrl)
          }

          "retain the client VRN" in {
            session(result).get(SessionKeys.clientVRN) shouldBe Some(vrn)
          }

          "retain the agent email" in {
            session(result).get(SessionKeys.notificationsEmail) shouldBe Some("an.email@host.com")
          }
        }

        "notification preference of yes is in session and verified agent email" should {

          lazy val result = {
            TestConfirmClientVrnController.redirect(FakeRequest().withSession(
              SessionKeys.preference -> "yes",
              SessionKeys.viewedDDInterrupt -> "true",
              SessionKeys.clientVRN -> vrn,
              SessionKeys.redirectUrl -> "/vat-through-software/account/change-something-about-vat",
              SessionKeys.verifiedAgentEmail -> "an.email@host.com"
            ))
          }

          "return status SEE_OTHER (303)" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsOrganisation)
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the manage vat frontend in session" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatCustomerDetailsUrl)
          }

          "retain the client VRN" in {
            session(result).get(SessionKeys.clientVRN) shouldBe Some(vrn)
          }

          "retain the agent email" in {
            session(result).get(SessionKeys.verifiedAgentEmail) shouldBe Some("an.email@host.com")
          }
        }

        "notification preference of yes is in session and no verified agent email" should {

          lazy val result = {
            TestConfirmClientVrnController.redirect(FakeRequest().withSession(
              SessionKeys.preference -> "yes",
              SessionKeys.clientVRN -> vrn,
              SessionKeys.viewedDDInterrupt -> "true",
              SessionKeys.redirectUrl -> "/vat-through-software/account/change-something-about-vat"
            ))
          }

          "return status SEE_OTHER (303)" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsOrganisation)
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the isEmailVerified action" in {
            redirectLocation(result) shouldBe Some(
              controllers.agent.routes.ConfirmEmailController.isEmailVerified.url)
          }
        }

        "notification preference is not in session" should {

          lazy val result = {
            TestConfirmClientVrnController.redirect(FakeRequest().withSession(
              SessionKeys.clientVRN -> vrn,
              SessionKeys.viewedDDInterrupt -> "true",
              SessionKeys.redirectUrl -> "/vat-through-software/account/change-something-about-vat"
            ))
          }

          "return status SEE_OTHER (303)" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsOrganisation)
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the capture preference controller" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.CapturePreferenceController.show().url)
          }
        }

        "a redirect url to a location which doesn't involve a vat change been provided" should {

          lazy val result = {
            TestConfirmClientVrnController.redirect(FakeRequest().withSession(
              SessionKeys.clientVRN -> vrn,
              SessionKeys.viewedDDInterrupt -> "true",
              SessionKeys.redirectUrl -> "/random-place"
            ))
          }

          "return status SEE_OTHER (303)" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsOrganisation)
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the provided url because preference is not required controller" in {
            redirectLocation(result) shouldBe Some("/random-place")
          }
        }

        "an empty redirect url has been provided" should {

          lazy val result = {
            TestConfirmClientVrnController.redirect(FakeRequest().withSession(
              SessionKeys.clientVRN -> vrn,
              SessionKeys.viewedDDInterrupt -> "true",
              SessionKeys.redirectUrl -> ""
            ))
          }

          "return status SEE_OTHER (303)" in {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsOrganisation)
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the the what to do page" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show.url)
          }
        }
      }
    }

    "redirect URL is not in session" should {

      lazy val result = {
        TestConfirmClientVrnController.redirect(FakeRequest().withSession(
          SessionKeys.clientVRN -> vrn,
          SessionKeys.viewedDDInterrupt -> "true",
          SessionKeys.notificationsEmail -> "an.email@host.com"
        ))
      }

      "return status SEE_OTHER (303)" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsOrganisation)
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to WhatToDo controller" in {
        redirectLocation(result) shouldBe Some(controllers.agent.routes.AgentHubController.show.url)
      }
    }

    "the DD interrupt value is not in session" should {

      lazy val result = {
        TestConfirmClientVrnController.redirect(FakeRequest().withSession(
          SessionKeys.clientVRN -> vrn,
          SessionKeys.notificationsEmail -> "an.email@host.com"
        ))
      }

      "return status SEE_OTHER (303)" in {
        mockAgentAuthorised()
        mockCustomerDetailsSuccess(customerDetailsOrganisation)
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the DD interrupt controller" in {
        redirectLocation(result) shouldBe Some(controllers.agent.routes.DDInterruptController.show.url)
      }
    }
  }
}
