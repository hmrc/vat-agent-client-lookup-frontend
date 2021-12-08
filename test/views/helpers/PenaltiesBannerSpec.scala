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

package views.templates.penalties

import messages.PenaltiesMessages.{multiplePenaltiesBannerLinkText, penaltiesBannerHeading, singlePenaltyBanerLinkText}
import models.penalties.PenaltiesSummary
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.helpers.PenaltiesBanner

class PenaltiesBannerSpec extends ViewBaseSpec {

  val injectedView: PenaltiesBanner = inject[PenaltiesBanner]
  val model: PenaltiesSummary = PenaltiesSummary.empty

  object Selectors {
    val notificationBanner = ".govuk-notification-banner"
    val heading = "h2"
    val penaltyInformation = ".govuk-notification-banner__content > div"
  }

  "The Penalties banner" when {

    "there are no active penalties" should {

      lazy val view = injectedView(model)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "be hidden" in {
        elementExtinct(Selectors.notificationBanner)
      }
    }

    "there are active penalties" when {

      "there are only penalty points" when {

        "there is one penalty point" should {

          lazy val view = injectedView(model.copy(noOfPoints = 1))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe "Total penalty points: 1"
          }

          "have a link to the penalties service" which {

            "has the correct text" in {
              elementText("a") shouldBe singlePenaltyBanerLinkText
            }

            "has the correct link destination" in {
              element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
            }
          }
        }

        "there is more than one penalty point" should {

          lazy val view = injectedView(model.copy(noOfPoints = 2))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe "Total penalty points: 2"
          }

          "have the correct link text" in {
            elementText("a") shouldBe multiplePenaltiesBannerLinkText
          }

          "has the correct link destination" in {
            element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
          }
        }

      }

      "there are only crystalised penalties" when {

        "there is one crystalised penalty" should {

          lazy val view = injectedView(model.copy(noOfPoints = 0, noOfCrystalisedPenalties = 1,
            crystalisedPenaltyAmountDue = 100.00))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe "Penalty amount to pay: £100"
          }

          "have a link to the penalties service" which {

            "has the correct text" in {
              elementText("a") shouldBe singlePenaltyBanerLinkText
            }

            "has the correct link destination" in {
              element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
            }
          }
        }

        "there is more than one crystalised penalty" should {

          lazy val view = injectedView(model.copy(noOfCrystalisedPenalties = 2, crystalisedPenaltyAmountDue = 200.99))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe "Penalty amount to pay: £200.99"
          }

          "have the correct link text" in {
            elementText("a") shouldBe multiplePenaltiesBannerLinkText
          }

          "has the correct link destination" in {
            element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
          }
        }

      }

      "there are only estimated penalties" when {

        "there is one estimated penalty" should {

          lazy val view = injectedView(model.copy(noOfEstimatedPenalties = 1, estimatedPenaltyAmount = 100.00))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe "Estimated penalty amount: £100"
          }

          "have a link to the penalties service" which {

            "has the correct text" in {
              elementText("a") shouldBe singlePenaltyBanerLinkText
            }

            "has the correct link destination" in {
              element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
            }
          }
        }

        "there is more than one estimated penalty" should {

          lazy val view = injectedView(model.copy(noOfEstimatedPenalties = 2, estimatedPenaltyAmount = 200.99))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe "Estimated penalty amount: £200.99"
          }

          "have the correct link text" in {
            elementText("a") shouldBe multiplePenaltiesBannerLinkText
          }

          "has the correct link destination" in {
            element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
          }
        }
      }

      "there are both crystalised penalties and penalty points, but no estimated penalties" should {

        lazy val view = injectedView(model.copy(noOfPoints = 1, noOfCrystalisedPenalties = 1, crystalisedPenaltyAmountDue = 100.00))
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct heading" in {
          elementText(Selectors.heading) shouldBe penaltiesBannerHeading
        }

        "have the correct penalty information" in {
          elementText(Selectors.penaltyInformation) shouldBe "Penalty amount to pay: £100 Total penalty points: 1"
        }

        "have the correct link text" in {
          elementText("a") shouldBe multiplePenaltiesBannerLinkText
        }

        "has the correct link destination" in {
          element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
        }
      }

      "there are both crystalised penalties and estimated penalties, but no penalty points" should {

        lazy val view = injectedView(model.copy(noOfCrystalisedPenalties = 1, crystalisedPenaltyAmountDue = 100.00,
          noOfEstimatedPenalties = 1, estimatedPenaltyAmount = 150.00))
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct heading" in {
          elementText(Selectors.heading) shouldBe penaltiesBannerHeading
        }

        "have the correct penalty information" in {
          elementText(Selectors.penaltyInformation) shouldBe
            "Penalty amount to pay: £100 Estimated further penalty amount: £150"
        }

        "have the correct link text" in {
          elementText("a") shouldBe multiplePenaltiesBannerLinkText
        }

        "has the correct link destination" in {
          element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
        }
      }

      "there are both estimated penalties and penalty points, but no crystalised penalties" should {

        lazy val view = injectedView(model.copy(noOfPoints = 1, noOfEstimatedPenalties = 1, estimatedPenaltyAmount = 150.00))
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct heading" in {
          elementText(Selectors.heading) shouldBe penaltiesBannerHeading
        }

        "have the correct penalty information" in {
          elementText(Selectors.penaltyInformation) shouldBe "Estimated penalty amount: £150 Total penalty points: 1"
        }

        "have the correct link text" in {
          elementText("a") shouldBe multiplePenaltiesBannerLinkText
        }

        "has the correct link destination" in {
          element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
        }
      }

      "there are crystalised penalties, estimated penalties and active points" should {

        lazy val view = injectedView(model.copy(noOfPoints = 1, noOfCrystalisedPenalties = 1, crystalisedPenaltyAmountDue = 100.00,
          noOfEstimatedPenalties = 1, estimatedPenaltyAmount = 150.00))
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct heading" in {
          elementText(Selectors.heading) shouldBe penaltiesBannerHeading
        }

        "have the correct penalty information" in {
          elementText(Selectors.penaltyInformation) shouldBe
            "Penalty amount to pay: £100 Estimated further penalty amount: £150 Total penalty points: 1"
        }

        "have the correct link text" in {
          elementText("a") shouldBe multiplePenaltiesBannerLinkText
        }

        "has the correct link destination" in {
          element("a").attr("href") shouldBe mockConfig.penaltiesFrontendUrl
        }
      }

    }
  }
}