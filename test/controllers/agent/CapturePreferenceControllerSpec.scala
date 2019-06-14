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

import assets.BaseTestConstants._
import audit.mocks.MockAuditingService
import audit.models.{NoPreferenceAuditModel, YesPreferenceAttemptedAuditModel}
import common.SessionKeys
import controllers.ControllerBaseSpec
import models.Agent
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterAll
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.Helpers._

class CapturePreferenceControllerSpec extends ControllerBaseSpec with MockAuditingService with BeforeAndAfterAll {

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockConfig.features.preferenceJourneyEnabled(true)
    mockConfig.features.whereToGoFeature(false)
  }

  val testRedirectUrl: String     = "/manage-vat-account"
  val testValidEmail: String      = "test@example.com"
  val testInvalidEmail: String    = "invalidEmail"
  val testYesPreference: String   = "yes"
  val testNoPreference: String    = "no"

  def target: CapturePreferenceController = new CapturePreferenceController(
    messagesApi,
    mockAgentOnlyAuthPredicate,
    mockPreferencePredicate,
    mockAuditingService,
    mockConfig
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "whatToDo feature switch is on" when {

        "client VRN is in session" should {

          lazy val result = {
            mockConfig.features.whereToGoFeature(true)
            target.show(request.withSession(
              SessionKeys.clientVRN -> "999999999"
            ))
          }

          "return 200" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.OK
          }

          "render capturePreference page" in {
            Jsoup.parse(bodyOf(result)).title() shouldBe "Would you like to receive email notifications of any changes you make?"
          }
        }

        "client VRN is not in session" when {

          "redirect URL is in session" should {

            lazy val result = {
              mockConfig.features.whereToGoFeature(true)
              target.show(request.withSession(
                SessionKeys.redirectUrl -> testRedirectUrl
              ))
            }

            "return 303" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.SEE_OTHER
            }

            s"redirect to ${controllers.agent.routes.SelectClientVrnController.show(testRedirectUrl).url}" in {
              redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show(testRedirectUrl).url)
            }
          }

          "redirect URL is not in session" should {

            lazy val result = {
              mockConfig.features.whereToGoFeature(true)
              target.show(request)
            }

            "return 303" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.SEE_OTHER
            }

            s"redirect to ${controllers.agent.routes.SelectClientVrnController.show().url}" in {
              redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show().url)
            }
          }
        }
      }

      "whatToDo feature switch is off" when {

        "there is no preference or verified email in session" should {

          lazy val result = target.show(request.withSession(
            SessionKeys.redirectUrl -> testRedirectUrl
          ))

          "return 200" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }
        }

        "there is a preference of 'no' in session" should {

          lazy val result = target.show(request.withSession(
            SessionKeys.redirectUrl -> testRedirectUrl,
            SessionKeys.preference -> "no"
          ))

          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Select Client VRN controller" in {
            redirectLocation(result) shouldBe
              Some(controllers.agent.routes.SelectClientVrnController.show(testRedirectUrl).url)
          }
        }

        "there is a verified email in session" should {

          lazy val result = target.show(request.withSession(
            SessionKeys.redirectUrl -> testRedirectUrl,
            SessionKeys.verifiedAgentEmail -> "pepsi-mac@gmail.com"
          ))

          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Select Client VRN controller" in {
            redirectLocation(result) shouldBe
              Some(controllers.agent.routes.SelectClientVrnController.show(testRedirectUrl).url)
          }
        }

        "there is a preference of 'Yes' and a notification email in session" should {

          lazy val result = target.show(request.withSession(
            SessionKeys.preference -> "yes",
            SessionKeys.notificationsEmail -> "pepsi-mac@test.com"
          ))
          lazy val document = Jsoup.parse(bodyOf(result))

          "return 200" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }

          "prepopulate the form with the preference" in {
            document.select("#yes_no-yes").attr("checked") shouldBe "checked"
          }

          "prepopulate the form with the email" in {
            document.select("#email").attr("value") shouldBe "pepsi-mac@test.com"
          }
        }
      }
    }

    "a user does not have a valid enrolment" should {

      lazy val result = target.show(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 403" in {
        mockAgentWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = target.submit(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 401" in {
        mockMissingBearerToken()
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "Calling the submit action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user enters the 'No' option" when {

        "whatToDo feature is enabled" when {

          "redirect URL is in session" should {

            lazy val testRequest = Agent[AnyContentAsFormUrlEncoded](arn)(request
              .withSession(SessionKeys.redirectUrl -> testRedirectUrl)
              .withFormUrlEncodedBody("yes_no" -> testNoPreference)
            )

            lazy val result = {
              mockConfig.features.whereToGoFeature(true)
              target.submit(testRequest)
            }

            "return 303" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to redirect URL" in {
              redirectLocation(result) shouldBe Some(testRedirectUrl)
            }

            "add the preference to the session" in {
              session(result).get(SessionKeys.preference) shouldBe Some(testNoPreference)
            }
          }

          "redirect URL is not in session" should {

            lazy val testRequest = Agent[AnyContentAsFormUrlEncoded](arn)(request
              .withFormUrlEncodedBody("yes_no" -> testNoPreference)
            )

            lazy val result = {
              mockConfig.features.whereToGoFeature(true)
              target.submit(testRequest)
            }

            "return 303" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.SEE_OTHER
            }

            s"redirect to ${mockConfig.manageVatCustomerDetailsUrl}" in {
              redirectLocation(result) shouldBe Some(mockConfig.manageVatCustomerDetailsUrl)
            }

            "add the preference to the session" in {
              session(result).get(SessionKeys.preference) shouldBe Some(testNoPreference)
            }
          }
        }

        "whatToDo feature is disabled" should {

          lazy val testRequest =
            Agent[AnyContentAsFormUrlEncoded](arn)(request
              .withSession(SessionKeys.redirectUrl -> testRedirectUrl)
              .withFormUrlEncodedBody("yes_no" -> testNoPreference))

          lazy val result = target.submit(testRequest)

          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the select client VRN controller" in {
            redirectLocation(result) shouldBe
              Some(controllers.agent.routes.SelectClientVrnController.show(testRedirectUrl).url)
          }

          "add the preference to the session" in {
            session(result).get(SessionKeys.preference) shouldBe Some(testNoPreference)
          }

          "audit the event" in {
            mockAgentAuthorised()
            await(target.submit(testRequest))
            verifyExtendedAudit(
              NoPreferenceAuditModel(arn),
              Some(controllers.agent.routes.CapturePreferenceController.submit().url)
            )
          }
        }
      }

      "the user enters the 'Yes' option and an email address" should {

        lazy val testRequest =
          Agent[AnyContentAsFormUrlEncoded](arn)(request
            .withSession(SessionKeys.redirectUrl -> testRedirectUrl)
            .withFormUrlEncodedBody("yes_no" -> testYesPreference, "email" -> testValidEmail))
        lazy val result = target.submit(testRequest)

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the confirm email controller" in {
          redirectLocation(result) shouldBe Some(controllers.agent.routes.ConfirmEmailController.show().url)
        }

        "add the preference to the session" in {
          session(result).get(SessionKeys.preference) shouldBe Some(testYesPreference)
        }

        "add the new email to the session" in {
          session(result).get(SessionKeys.notificationsEmail) shouldBe Some(testValidEmail)
        }

        "audit the event" in {
          mockAgentAuthorised()
          await(target.submit(testRequest))
          verifyExtendedAudit(
            YesPreferenceAttemptedAuditModel(arn, testValidEmail),
            Some(controllers.agent.routes.CapturePreferenceController.submit().url)
          )
        }
      }

      "the user enters invalid data" should {

        lazy val result = target.submit(request
          .withSession(SessionKeys.redirectUrl -> testRedirectUrl)
          .withFormUrlEncodedBody("yes_no" -> testYesPreference, "email" -> testInvalidEmail))

        "reload the page with errors" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.BAD_REQUEST
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user does not have a valid enrolment" should {

      lazy val result = target.submit(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 403" in {
        mockAgentWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = target.submit(request.withSession(
        SessionKeys.redirectUrl -> testRedirectUrl
      ))

      "return 401" in {
        mockMissingBearerToken()
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
