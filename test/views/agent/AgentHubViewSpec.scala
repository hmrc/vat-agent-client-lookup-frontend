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

package views.agent

import java.time.LocalDate

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec
import assets.BaseTestConstants.{arn, vrn}
import assets.CustomerDetailsTestConstants._
import assets.messages.{AgentHubMessages => Messages}
import assets.messages.partials.CovidPartialMessages
import assets.messages.partials.{SignUpPartialMessages, _}
import views.html.agent.AgentHubView

class AgentHubViewSpec extends ViewBaseSpec {

  val injectedView: AgentHubView = inject[AgentHubView]

  "AgentHubPage" when {

    val date: LocalDate = LocalDate.parse("2018-05-01")

    "the user is a valid agent for an opted-in client with covid feature switch disabled" should {
      lazy val view = injectedView(customerDetailsFnameOnly, vrn, date, preCovidDeadline = true)(request,messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display the covid partial" in {
        mockConfig.features.displayCovidMessage(false)
        elementExtinct("#covid-partial")
      }
    }

    "the user is a valid agent for an opted-in client with covid feature switch enabled pre the end of June 2020" should {

      lazy val view = injectedView(customerDetailsFnameOnly, vrn, date, preCovidDeadline = false)(request,messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the covid partial" in {
        mockConfig.features.displayCovidMessage(true)
        elementText("#covid-partial strong") shouldBe CovidPartialMessages.headingPreEnd
      }

      "have the correct title" in {
        document.title shouldBe Messages.title
      }

      "display the correct heading" in {
        elementText("h1") shouldBe Messages.heading
      }

      "display the correct client details" in {
        elementText(".form-hint") should include(Messages.vatNo(vrn))
        elementText(".form-hint") should include(customerDetailsFnameOnly.clientName)
        elementText(".form-hint > a") shouldBe Messages.changeClient
      }

      "has the correct URL for changing client" in {
        element(".form-hint > a").attr("href") shouldBe controllers.agent.routes.ConfirmClientVrnController.changeClient().url
      }

      "have a breadcrumb link to agent services" in {
        elementText("#breadcrumb-asa") shouldBe Messages.agentServicesAccount
        element("#breadcrumb-asa").attr("href") shouldBe mockConfig.agentServicesUrl
      }

      "display the client details partial" in {
        elementText("#client-details > h2") shouldBe ClientDetailsPartialMessages.heading
      }

      "display the VAT Returns partial" in {
        elementText("#vat-returns > h2") shouldBe VatReturnsPartialMessages.heading
      }

      "display the VAT Certificate partial" in {
        elementText("#vat-certificate > h2") shouldBe VatCertificatePartialMessages.heading
      }

      "display the Opt Out partial" in {
        elementText("#opt-out > h3") shouldBe OptOutForMTDVATMessages.title
      }

      "display the Cancel VAT registration partial" in {
        elementText("#cancel-vat > h3") shouldBe RegistrationPartialMessages.cancelRegistrationTitle
      }

      "not display the sign-up partial" in {
        elementExtinct("#sign-up-partial")
      }
    }

    "the user is a valid agent for an opted-in client with covid feature switch enabled post the end of June 2020" should {

      lazy val view = injectedView(customerDetailsFnameOnly, vrn, date, preCovidDeadline = true)(request, messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the covid partial" in {
        mockConfig.features.displayCovidMessage(true)
        elementText("#covid-partial strong") shouldBe CovidPartialMessages.headingPostEnd
      }
    }

    "the user is an agent for an opted out client" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = injectedView(customerDetailsOptedOut, vrn, date, preCovidDeadline = true)(request,messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display the opt-out partial" in {
        elementExtinct("#opt-out")
      }

      "display the sign-up partial" in {
        elementText("#sign-up-partial > h3") shouldBe SignUpPartialMessages.signUpLinkText
      }
    }

    "the user is an agent for a 'Non-Digital' client" should {

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = injectedView(customerDetailsNonDigital, vrn, date, preCovidDeadline = true)(request,messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display the opt-out partial" in {
        elementExtinct("#opt-out")
      }

      "display the sign-up partial" in {
        elementText("#sign-up-partial > h3") shouldBe SignUpPartialMessages.signUpLinkText
      }
    }

    "the user is an agent for a deregistered client with a dereg date in the past" should {
      val otherDate: LocalDate = LocalDate.parse("2020-01-01")

      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = injectedView(customerDetailsAllInfo, vrn, otherDate, preCovidDeadline = true)(request,messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the Cancel VAT registration historic partial" in {
        elementText("#cancel-vat > h3") shouldBe RegistrationPartialMessages.historicDeregTitle
      }
    }

    "the user is an agent for a deregistered client with a dereg date in the future" should {

      val date: LocalDate = LocalDate.parse("2010-01-01")
      lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      lazy val view = injectedView(customerDetailsAllInfo, vrn, date, preCovidDeadline = true)(request,messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the 'cancel vat registration' partial with the correct future of historic date" in {
        elementText("#cancel-vat > h3") shouldBe RegistrationPartialMessages.futureDeregisterTitle
      }
    }
  }

}
