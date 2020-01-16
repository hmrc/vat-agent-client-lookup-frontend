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

package views.agent.partials


import java.time.LocalDate
import java.time.format.DateTimeFormatter

import assets.BaseTestConstants.vrn
import assets.messages.partials.RegistrationPartialMessages
import models.{Deregistered, Registered, User}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.partials.registrationPartial
import assets.CustomerDetailsTestConstants._
import common.SessionKeys
import play.api.i18n.Lang
import utils.ImplicitDateFormatter._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class RegistrationPartialSpec extends ViewBaseSpec {

  "Rendering the partial" when {

    "client is registered" when {

      "client is not pending deregistration" when {

        "agent has not entered their contact preference" should {

          lazy val view = registrationPartial(customerDetailsNoInfo, toLocalDate("2019-01-01"))(messages, mockConfig, Lang("en"), user)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "display a section for cancelling registration" which {

            s"should have the correct title of ${RegistrationPartialMessages.cancelRegistrationTitle}" in {
              elementText("h3") shouldBe RegistrationPartialMessages.cancelRegistrationTitle
            }

            s"link to ${controllers.agent.routes.CapturePreferenceController.show().url}" in {
              element("h3 > a").attr("href") shouldBe controllers.agent.routes.CapturePreferenceController.show().url
            }

            s"have correct content of ${RegistrationPartialMessages.cancelRegistrationContent}" in {
              elementText("p") shouldBe RegistrationPartialMessages.cancelRegistrationContent
            }
          }
        }

        "agent has entered their contact preference" should {

          lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
            .withSession(SessionKeys.verifiedAgentEmail -> "exampleemail@email.com")
          lazy val testUser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(testGetRequest)
          lazy val view = registrationPartial(customerDetailsNoInfo, toLocalDate("2019-01-01"))(messages, mockConfig, Lang("en"), testUser)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "display a section for cancelling registration" which {

            s"should have the correct title of ${RegistrationPartialMessages.cancelRegistrationTitle}" in {
              elementText("h3") shouldBe RegistrationPartialMessages.cancelRegistrationTitle
            }

            s"link to ${mockConfig.cancelRegistrationUrl}" in {
              element("h3 > a").attr("href") shouldBe mockConfig.cancelRegistrationUrl
            }

            s"have correct content of ${RegistrationPartialMessages.cancelRegistrationContent}" in {
              elementText("p") shouldBe RegistrationPartialMessages.cancelRegistrationContent
            }
          }
        }
      }

      "client is pending deregistration" should {

        lazy val view = registrationPartial(customerDetailsPendingDeregestrationNoInfo, toLocalDate("2019-01-01"))(messages, mockConfig, Lang("en"), user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display a section for pending deregistration" which {

          lazy val view=registrationPartial(customerDetailsPendingDeregestrationNoInfo, toLocalDate("2019-01-01"))(messages, mockConfig, Lang("en"), user)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          s"should have the correct title of ${RegistrationPartialMessages.pendingRegistrationTitle}" in {
            elementText("h3") shouldBe RegistrationPartialMessages.pendingRegistrationTitle
          }

          s"have the correct content" in {
            elementText("p") shouldBe RegistrationPartialMessages.pendingRegistrationContent
          }
        }
      }
    }

    "client is not registered" when {

      "the effectDateOfCancellation is before the currentDate" should {

        "display the historic dereg partial" which {

          lazy val view = registrationPartial(customerDetailsAllInfo, toLocalDate("2019-01-02"))(messages, mockConfig, Lang("en"), user)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          s"should have the correct title of ${RegistrationPartialMessages.historicDeregTitle}" in {
            elementText("h3") shouldBe RegistrationPartialMessages.historicDeregTitle
          }

          s"have correct content of ${RegistrationPartialMessages.historicDeregContent}" in {
            elementText("p") shouldBe RegistrationPartialMessages.historicDeregContent
          }

          s"have a link to ${mockConfig.onlineAgentServicesUrl}" in {
            element("p > a").attr("href") shouldBe mockConfig.onlineAgentServicesUrl
          }
        }
      }

      "client has a deregister date in the future" should {

        "display a section for future registration" which {

          lazy val view = registrationPartial(customerDetailsFutureDeregisterOptedOut, toLocalDate("2019-01-01"))(messages, mockConfig, Lang("en"), user)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          s"should have the correct title of ${RegistrationPartialMessages.futureDeregisterTitle}" in {
            elementText("h3") shouldBe RegistrationPartialMessages.futureDeregisterTitle
          }

          s"have correct content of ${RegistrationPartialMessages.futureDeregisterContent}" in {
            elementText("p") shouldBe RegistrationPartialMessages.futureDeregisterContent
          }

          s"link with text of ${RegistrationPartialMessages.futureDeregLink}" in {
            element("a").text shouldBe RegistrationPartialMessages.futureDeregLink
          }

          s"link to ${mockConfig.onlineAgentServicesUrl}" in {
            element("a").attr("href") shouldBe mockConfig.onlineAgentServicesUrl
          }
        }
      }
    }


  }
}
