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

package views.agent

import assets.BaseTestConstants.vrn
import assets.CustomerDetailsTestConstants._
import assets.HubViewModelTestConstants.{hubViewModel, hubViewModelBlueBox}
import assets.PenaltiesConstants.penaltiesSummaryAsModel
import assets.messages.partials._
import assets.messages.{AgentHubMessages => Messages}
import messages.partials.{NextPaymentPartialMessages, PenaltiesTileMessages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.AgentHubView

class AgentHubViewSpec extends ViewBaseSpec {

  val injectedView: AgentHubView = inject[AgentHubView]

  "AgentHubPage" when {

    "the user is a valid agent for an opted-in client" should {

      lazy val view = injectedView(hubViewModel(customerDetailsFnameOnly))(messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe Messages.title
      }

      "have a breadcrumb link to agent services" in {
        elementText("#agent-services-breadcrumb") shouldBe Messages.agentServicesAccount
        element("#agent-services-breadcrumb").attr("href") shouldBe mockConfig.agentServicesUrl
      }

      "display the correct heading" in {
        elementText("h1") shouldBe Messages.heading
      }

      "display the correct client details" in {
        elementText(".govuk-caption-m") should include(Messages.vatNo(vrn))
        elementText("span.govuk-caption-m:nth-of-type(2)") should include(customerDetailsFnameOnly.clientName)
        elementText(".govuk-caption-m > a") shouldBe Messages.changeClient
      }

      "has the correct URL for changing client" in {
        element("#change-client-link").attr("href") shouldBe
          controllers.agent.routes.ConfirmClientVrnController.changeClient.url
      }

      "display the next payment due partial" in {
        elementText("#next-payment-heading") shouldBe NextPaymentPartialMessages.heading
      }

      "display the next return due partial" in {
        elementText("#vat-returns > h2") shouldBe NextReturnPartialMessages.heading
      }

      "display the payment history partial" in {
        elementText("#history-title") shouldBe HistoryPartialMessages.heading
      }

      "display the Manage VAT heading" in {
        elementText("#manage-vat-heading") shouldBe Messages.manageVat
      }

      "display the correct warning message" in {
        elementText("#noDDClient") shouldBe Messages.noDDclient
      }

      "display the client details partial" in {
        elementText("#client-details > h3") shouldBe ClientDetailsPartialMessages.linkText
      }

      "display the VAT Certificate partial" in {
        elementText("#certificate-link") shouldBe VatCertificatePartialMessages.linkText
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

    "the user has the blueBox value in session" should {

      lazy val view = injectedView(hubViewModelBlueBox(customerDetailsFnameOnly))(messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have a notification banner" that {

        "has the correct title" in {
          elementText("#govuk-notification-banner-title") shouldBe Messages.notificationBannerTitle
        }

        "has the correct first sentence" in {
          elementText("#noti-p1") shouldBe Messages.notificationBannerP1
        }

        "has the correct second sentence" in {
          elementText("#noti-p2") shouldBe Messages.notificationBannerP2
        }

        "has the correct third sentence" in {
          elementText("#noti-p3") shouldBe Messages.notificationBannerP3
        }

      }
    }

    "the user is an agent for an opted out client" should {

      lazy val view = injectedView(hubViewModel(customerDetailsOptedOut))(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display the opt-out partial" in {
        elementExtinct("#opt-out")
      }

      "display the sign-up partial" in {
        elementText("#sign-up-heading") shouldBe SignUpPartialMessages.signUpLinkText
      }
    }

    "the user is an agent for a hybrid user" should {

      lazy val view = injectedView(hubViewModel(customerDetailsHybrid))(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display the Next Payment partial" in {
        elementExtinct("#next-payment")
      }
    }

    "the user is an agent for a 'Non-Digital' client" should {

      lazy val view = injectedView(hubViewModel(customerDetailsNonDigital))(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display the opt-out partial" in {
        elementExtinct("#opt-out")
      }

      "display the sign-up partial" in {
        elementText("#sign-up-heading") shouldBe SignUpPartialMessages.signUpLinkText
      }
    }

    "the user is an agent for a deregistered client with a dereg date in the past" should {

      lazy val view = injectedView(hubViewModel(customerDetailsAllInfo))(messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the Cancel VAT registration historic partial" in {
        elementText("#cancel-vat > h3") shouldBe RegistrationPartialMessages.historicDeregTitle
      }
    }

    "the user is an agent for a deregistered client with a dereg date in the future" should {

      lazy val view = injectedView(hubViewModel(customerDetailsAllInfo))(messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the 'cancel vat registration' partial with the correct future of historic date" in {
        elementText("#cancel-vat > h3") shouldBe RegistrationPartialMessages.futureDeregisterTitle
      }
    }

    "the user is an agent for a client who has penalties" should {
      lazy val view = injectedView(hubViewModel(customerDetailsAllInfo, penalties = Some(penaltiesSummaryAsModel)))(messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the 'penalties tile' partial" in {
        elementText("#penalties-tile > h3") shouldBe PenaltiesTileMessages.title
        elementText("#penalties-tile > p") shouldBe PenaltiesTileMessages.description
      }
    }
  }

}
