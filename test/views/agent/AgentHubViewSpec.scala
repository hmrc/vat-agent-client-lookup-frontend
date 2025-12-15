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

package views.agent

import assets.BaseTestConstants.vrn
import assets.CustomerDetailsTestConstants._
import assets.HubViewModelTestConstants.hubViewModel
import assets.PenaltiesConstants.{penaltiesSummaryAsModel, penaltiesSummaryAsModelNoPenalties}
import assets.messages.partials._
import assets.messages.{AgentHubMessages => Messages}
import messages.PenaltiesMessages._
import messages.partials.{NextPaymentPartialMessages, PenaltiesTileMessages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.AgentHubView
import java.time.LocalDate

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

      "display the Cancel VAT registration partial" in {
        elementText("#cancel-vat > h3") shouldBe RegistrationPartialMessages.cancelRegistrationTitle
      }

    }

    "the user is an agent for a hybrid user" should {

      lazy val view = injectedView(hubViewModel(customerDetailsHybrid))(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not display the Next Payment partial" in {
        elementExtinct("#next-payment")
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

      "display the Penalties notification banner" which {

        lazy val penaltiesBanner = element(".govuk-notification-banner")

        "has the correct heading" in {
          penaltiesBanner.select("#govuk-notification-banner-title-penalties-banner").text shouldBe penaltiesBannerHeading
        }

        "has content relating to the number of penalties the user has" in {
          penaltiesBanner.select(".govuk-notification-banner__content > div").text shouldBe
            "Penalty amount to pay: £54.32 Estimated further penalty amount: £123.45 Total penalty points: 3"
        }

        "has a link to the penalties service" which {

          "has the correct text" in {
            penaltiesBanner.select("a").text shouldBe multiplePenaltiesBannerLinkText
          }

          "has the correct link destination" in {
            penaltiesBanner.select("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
          }
        }
      }

      "not display the penalties coming banner heading" in {
        elementExtinct("#penalties-coming-banner-heading")
      }

      "not display the penalties coming banner content" in {
        elementExtinct("#penalties-coming-first-para")
        elementExtinct("#penalties-coming-second-para")
        elementExtinct("#penalties-coming-third-para")
      }

      "not display the penalties coming banner link" in {
        elementExtinct("#penalties-coming-link")
      }
    }

    "the user has no penalties" should {

      lazy val view = injectedView(hubViewModel(customerDetailsAllInfo, penalties = Some(penaltiesSummaryAsModelNoPenalties)))(messages,mockConfig,user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      lazy val penaltiesSection = "#view-penalties-details"

      "not display the penalties and appeal section" in {
        elementExtinct(penaltiesSection)
      }

      "not display any penalty information" in {
        elementExtinct("#crystalised-penalties-content")
        elementExtinct("#estimated-penalties-content")
        elementExtinct("#crystalised-and-estimated-penalties-content")
        elementExtinct("#penalty-points-content")
      }

      "not display a link to the penalties service" in {
        elementExtinct("#penalties-service-link")
      }

      "display the penalties coming banner" which {

        "have the correct heading" in {
          elementText(".govuk-notification-banner__heading") shouldBe penaltiesComingBannerHeading
        }

        "have the correct first paragraph" in {
          elementText("#penalties-coming-first-para") shouldBe penaltiesComingBannerParaOne
        }

        "have the correct second paragraph" in {
          elementText("#penalties-coming-second-para") shouldBe penaltiesComingBannerParaTwo
        }

        "have the correct third paragraph" in {
          elementText("#penalties-coming-third-para") shouldBe penaltiesComingBannerParaThree
        }

        "have the correct link" which {

          "has the correct link text" in {
            elementText("#penalties-coming-link") shouldBe penaltiesComingBannerLinkText
          }

          "has the correct link location" in {
            element("#penalties-coming-link").attr("href") shouldBe mockConfig.penaltiesChangesUrl
          }
        }
      }
    }

    "Render VAT Payment on Account section" should {

      lazy val view = injectedView(hubViewModel(customerDetailsAllInfo))(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)
      "display the VAT Payment on Account section if client has POAActiveUntil and POA Feature is enabled" in {
        mockConfig.features.poaActiveFeature(true)
        elementText("#vat-payment-on-account h3") shouldBe Messages.poalinkText
        element("#poa-link").attr("href") shouldBe mockConfig.vatPaymentOnAccountUrl
        elementText("#poa-body") shouldBe Messages.poalinkInfo
      }

      "doesn't display the VAT Payment on Account section if client doesn't have POAActiveUntil and POA Feature is enabled" in {
        lazy val view = injectedView(hubViewModel(customerDetailsAllInfo).copy(isPoaActiveForCustomer = false))(messages, mockConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)
        mockConfig.features.poaActiveFeature(true)
        document.select("#vat-payment-on-account h3") should be(empty)
        document.select("#poa-link").attr("href") should be(empty)
        document.select("#poa-body") should be(empty)
      }

      "doesn't display the VAT Payment on Account section if POA feature is disabled" in {
        mockConfig.features.poaActiveFeature(false)
        lazy val view = injectedView(hubViewModel(customerDetailsAllInfo))(messages, mockConfig, user)
        val renderedView = view.body
        lazy implicit val document: Document = Jsoup.parse(renderedView)
        document.select("#vat-payment-on-account h3") should be(empty)
        document.select("#poa-link").attr("href") should be(empty)
        document.select("#poa-body") should be(empty)
      }
    }

    "Render Annual Accounting section" should {
      "display the Annual Accounting section if feature enabled and user is AA" in {
        mockConfig.features.annualAccountingFeature(true)
        mockConfig.features.poaActiveFeature(true)
        val view = injectedView(hubViewModel(customerDetailsAllInfo).copy(isAnnualAccountingCustomer = true))(messages, mockConfig, user)
        implicit val document: Document = Jsoup.parse(view.body)
        elementText("#annual-accounting h3") shouldBe Messages.aaLinkText
        element("#aa-link").attr("href") shouldBe mockConfig.annualAccountingUrl
        elementText("#aa-body") shouldBe Messages.aaLinkInfo

        document.select("#vat-payment-on-account h3") should be(empty)
      }

      "fall back to POA tile when AA feature enabled but user not AA and POA enabled" in {
        mockConfig.features.annualAccountingFeature(true)
        mockConfig.features.poaActiveFeature(true)
        val view = injectedView(hubViewModel(customerDetailsAllInfo).copy(isAnnualAccountingCustomer = false))(messages, mockConfig, user)
        implicit val document: Document = Jsoup.parse(view.body)
        elementText("#vat-payment-on-account h3") shouldBe Messages.poalinkText
        element("#poa-link").attr("href") shouldBe mockConfig.vatPaymentOnAccountUrl
        elementText("#poa-body") shouldBe Messages.poalinkInfo
        document.select("#annual-accounting h3") should be(empty)
      }

      "not display AA or POA when both features disabled" in {
        mockConfig.features.annualAccountingFeature(false)
        mockConfig.features.poaActiveFeature(false)
        val view = injectedView(hubViewModel(customerDetailsAllInfo).copy(isAnnualAccountingCustomer = true))(messages, mockConfig, user)
        val renderedView = view.body
        implicit val document: Document = Jsoup.parse(renderedView)
        document.select("#annual-accounting h3") should be(empty)
        document.select("#aa-link").attr("href") should be(empty)
        document.select("#aa-body") should be(empty)
        document.select("#vat-payment-on-account h3") should be(empty)
        document.select("#poa-link").attr("href") should be(empty)
        document.select("#poa-body") should be(empty)
      }
    }

    "Render AA changed notification" should {
      "display the AA changed content with date and link when feature enabled" in {
        mockConfig.features.annualAccountingFeature(true)
        val changedOn = LocalDate.parse("2025-03-01")
        val view = injectedView(hubViewModel(customerDetailsAllInfo)
          .copy(isAnnualAccountingCustomer = true, annualAccountingChangedOn = Some(changedOn)))(messages, mockConfig, user)
        implicit val document: Document = Jsoup.parse(view.body)
        elementText("#aa-changed-information") should include(messages("agentHub.annual_accounting.alert.message.prefix"))
        elementText("#aa-changed-information") should include("1 March 2025")
        document.select("#aa-changed-information a").text shouldBe messages("agentHub.annual_accounting.alert.link") + "."
        document.select("#aa-changed-information a").attr("href") shouldBe mockConfig.annualAccountingUrl
      }
    }

    "Render AA overdue notification" should {
      "display the AA overdue content with link to what you owe" in {
        mockConfig.features.annualAccountingFeature(true)
        val view = injectedView(hubViewModel(customerDetailsAllInfo)
          .copy(isAnnualAccountingCustomer = true, isAnnualAccountingPaymentOverdue = true))(messages, mockConfig, user)
        implicit val document: Document = Jsoup.parse(view.body)
        elementText("#aa-overdue-information") should include(messages("agentHub.annual_accounting.overdue.message"))
        document.select("#aa-overdue-information a").text shouldBe messages("agentHub.annual_accounting.overdue.link") + "."
        document.select("#aa-overdue-information a").attr("href") shouldBe mockConfig.whatYouOweUrl
      }

      "not display AA overdue content when feature disabled" in {
        mockConfig.features.annualAccountingFeature(false)
        val view = injectedView(hubViewModel(customerDetailsAllInfo)
          .copy(isAnnualAccountingCustomer = true, isAnnualAccountingPaymentOverdue = true))(messages, mockConfig, user)
        implicit val document: Document = Jsoup.parse(view.body)
        document.select("#aa-overdue-information") should be(empty)
        document.select("#vat-gov-banner-alerts") should be(empty)
      }
    }
    "Render VAT Payment on Account section and Penalties together" should {

      lazy val view = injectedView(hubViewModel(customerDetailsAllInfo, penalties = Some(penaltiesSummaryAsModelNoPenalties)))(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)
      "display the VAT Payment on Account section if client has POAActiveUntil and POA Feature is enabled and penalties" in {
        mockConfig.features.poaActiveFeature(true)
        elementText("#vat-payment-on-account h3") shouldBe Messages.poalinkText
        element("#poa-link").attr("href") shouldBe mockConfig.vatPaymentOnAccountUrl
        elementText("#poa-body") shouldBe Messages.poalinkInfo
        elementText("#penalties-coming-first-para") shouldBe penaltiesComingBannerParaOne
        elementText("#penalties-coming-second-para") shouldBe penaltiesComingBannerParaTwo
        elementText("#penalties-coming-third-para") shouldBe penaltiesComingBannerParaThree
        elementText("#penalties-coming-link") shouldBe penaltiesComingBannerLinkText
      }

      "doesn't display the VAT Payment on Account section if client doesn't have POAActiveUntil and POA Feature is enabled but displays penalties" in {
        lazy val view = injectedView(hubViewModel(customerDetailsAllInfo, penalties = Some(penaltiesSummaryAsModelNoPenalties))
          .copy(isPoaActiveForCustomer = false))(messages, mockConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)
        mockConfig.features.poaActiveFeature(true)
        document.select("#vat-payment-on-account h3") should be(empty)
        document.select("#poa-link").attr("href") should be(empty)
        document.select("#poa-body") should be(empty)
        elementText("#penalties-coming-first-para") shouldBe penaltiesComingBannerParaOne
        elementText("#penalties-coming-second-para") shouldBe penaltiesComingBannerParaTwo
        elementText("#penalties-coming-third-para") shouldBe penaltiesComingBannerParaThree
        elementText("#penalties-coming-link") shouldBe penaltiesComingBannerLinkText
      }

      "doesn't display the VAT Payment on Account section if POA feature is disabled and does not display penalties" in {
        mockConfig.features.poaActiveFeature(false)
        lazy val view = injectedView(hubViewModel(customerDetailsAllInfo))(messages, mockConfig, user)
        val renderedView = view.body
        lazy implicit val document: Document = Jsoup.parse(renderedView)
        document.select("#vat-payment-on-account h3") should be(empty)
        document.select("#poa-link").attr("href") should be(empty)
        document.select("#poa-body") should be(empty)
        elementExtinct("#crystalised-penalties-content")
        elementExtinct("#estimated-penalties-content")
        elementExtinct("#crystalised-and-estimated-penalties-content")
        elementExtinct("#penalty-points-content")
      }
    }
  }
}


