/*
 * Copyright 2023 HM Revenue & Customs
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

import messages.PenaltiesMessages._
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
    val heading = "#govuk-notification-banner-title-penalties-banner"
    val penaltiesComingTitle = ".govuk-notification-banner__title"
    val penaltiesComingHeading = ".govuk-notification-banner__heading"
    val paraOne = "#penalties-coming-first-para"
    val paraTwo = "#penalties-coming-second-para"
    val paraThree = "#penalties-coming-third-para"
    val penaltiesServiceLink = "#penalties-service-link"
    val crystalisedPenaltiesContent = "#crystalised-penalties-content"
    val estimatedPenaltiesContent = "#estimated-penalties-content"
    val crystalisedAndEstimatedPenaltiesContent = "#crystalised-and-estimated-penalties-content"
    val penaltiesPointsContent = "#penalty-points-content"
    val penaltiesComingLink = "#penalties-coming-link"
    val penaltyInformation = ".govuk-notification-banner__content > div"
  }

  "The Penalties banner" when {

    "the Penalty Summary field is None" should {

      lazy val view = injectedView(None)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct banner title" in {
        elementText(Selectors.penaltiesComingTitle) shouldBe penaltiesComingBannerTitle
      }

      "have the correct heading" in {
        elementText(Selectors.penaltiesComingHeading) shouldBe penaltiesComingBannerHeading
      }

      "have the correct first paragraph" in {
        elementText(Selectors.paraOne) shouldBe penaltiesComingBannerParaOne
      }

      "have the correct second paragraph" in {
        elementText(Selectors.paraTwo) shouldBe penaltiesComingBannerParaTwo
      }

      "have the correct third paragraph" in {
        elementText(Selectors.paraThree) shouldBe penaltiesComingBannerParaThree
      }

      "have the correct link" which {

        "has the correct link text" in {
          elementText(Selectors.penaltiesComingLink) shouldBe penaltiesComingBannerLinkText
        }

        "has the correct link location" in {
          element(Selectors.penaltiesComingLink).attr("href") shouldBe mockConfig.penaltiesChangesUrl
        }
      }

      "not display any penalty information" in {
        elementExtinct(Selectors.crystalisedPenaltiesContent)
        elementExtinct(Selectors.estimatedPenaltiesContent)
        elementExtinct(Selectors.crystalisedAndEstimatedPenaltiesContent)
        elementExtinct(Selectors.penaltiesPointsContent)
      }

      "not display a link to the penalties service" in {
        elementExtinct(Selectors.penaltiesServiceLink)
      }
    }

    "there is a Penalty Summary" when {

      "there are no active penalties" should {

        lazy val view = injectedView(Some(model))
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct banner title" in {
          elementText(Selectors.penaltiesComingTitle) shouldBe penaltiesComingBannerTitle
        }

        "have the correct heading" in {
          elementText(Selectors.penaltiesComingHeading) shouldBe penaltiesComingBannerHeading
        }

        "have the correct first paragraph" in {
          elementText(Selectors.paraOne) shouldBe penaltiesComingBannerParaOne
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paraTwo) shouldBe penaltiesComingBannerParaTwo
        }

        "have the correct third paragraph" in {
          elementText(Selectors.paraThree) shouldBe penaltiesComingBannerParaThree
        }

        "have the correct link" which {

          "has the correct link text" in {
            elementText(Selectors.penaltiesComingLink) shouldBe penaltiesComingBannerLinkText
          }

          "has the correct link location" in {
            element(Selectors.penaltiesComingLink).attr("href") shouldBe mockConfig.penaltiesChangesUrl
          }
        }

        "not display any penalty information" in {
          elementExtinct(Selectors.crystalisedPenaltiesContent)
          elementExtinct(Selectors.estimatedPenaltiesContent)
          elementExtinct(Selectors.crystalisedAndEstimatedPenaltiesContent)
          elementExtinct(Selectors.penaltiesPointsContent)
        }

        "not display a link to the penalties service" in {
          elementExtinct(Selectors.penaltiesServiceLink)
        }
      }

      "there are active penalties" when {

        "there are only penalty points" when {

          "there is one penalty point" should {

            lazy val view = injectedView(Some(model.copy(noOfPoints = 1)))
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct heading" in {
              elementText(Selectors.heading) shouldBe penaltiesBannerHeading
            }

            "have the correct penalty information" in {
              elementText(Selectors.penaltyInformation) shouldBe "Total penalty points: 1"
            }

            "have a link to the penalties service" which {

              "has the correct text" in {
                elementText(Selectors.penaltiesServiceLink) shouldBe singlePenaltyBannerLinkText
              }

              "has the correct link destination" in {
                element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
              }
            }

            "not display the penalties coming banner heading" in {
              elementExtinct(Selectors.penaltiesComingHeading)
            }

            "not display the penalties coming banner content" in {
              elementExtinct(Selectors.paraOne)
              elementExtinct(Selectors.paraTwo)
              elementExtinct(Selectors.paraThree)
            }

            "not display the penalties coming banner link" in {
              elementExtinct(Selectors.penaltiesComingLink)
            }
          }

          "there is more than one penalty point" should {

            lazy val view = injectedView(Some(model.copy(noOfPoints = 2)))
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct heading" in {
              elementText(Selectors.heading) shouldBe penaltiesBannerHeading
            }

            "have the correct penalty information" in {
              elementText(Selectors.penaltyInformation) shouldBe "Total penalty points: 2"
            }

            "have the correct link text" in {
              elementText(Selectors.penaltiesServiceLink) shouldBe multiplePenaltiesBannerLinkText
            }

            "has the correct link destination" in {
              element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
            }

            "not display the penalties coming banner heading" in {
              elementExtinct(Selectors.penaltiesComingHeading)
            }

            "not display the penalties coming banner content" in {
              elementExtinct(Selectors.paraOne)
              elementExtinct(Selectors.paraTwo)
              elementExtinct(Selectors.paraThree)
            }

            "not display the penalties coming banner link" in {
              elementExtinct(Selectors.penaltiesComingLink)
            }
          }
        }

        "there are only crystalised penalties" when {

          "there is one crystalised penalty" should {

            lazy val view = injectedView(Some(model.copy(noOfPoints = 0, noOfCrystalisedPenalties = 1,
              crystalisedPenaltyAmountDue = 100.00)))
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct heading" in {
              elementText(Selectors.heading) shouldBe penaltiesBannerHeading
            }

            "have the correct penalty information" in {
              elementText(Selectors.penaltyInformation) shouldBe "Penalty amount to pay: £100"
            }

            "have a link to the penalties service" which {

              "has the correct text" in {
                elementText(Selectors.penaltiesServiceLink) shouldBe singlePenaltyBannerLinkText
              }

              "has the correct link destination" in {
                element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
              }
            }

            "not display the penalties coming banner heading" in {
              elementExtinct(Selectors.penaltiesComingHeading)
            }

            "not display the penalties coming banner content" in {
              elementExtinct(Selectors.paraOne)
              elementExtinct(Selectors.paraTwo)
              elementExtinct(Selectors.paraThree)
            }

            "not display the penalties coming banner link" in {
              elementExtinct(Selectors.penaltiesComingLink)
            }
          }

          "there is more than one crystalised penalty" should {

            lazy val view = injectedView(Some(model.copy(noOfCrystalisedPenalties = 2, crystalisedPenaltyAmountDue = 200.99)))
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct heading" in {
              elementText(Selectors.heading) shouldBe penaltiesBannerHeading
            }

            "have the correct penalty information" in {
              elementText(Selectors.penaltyInformation) shouldBe "Penalty amount to pay: £200.99"
            }

            "have the correct link text" in {
              elementText(Selectors.penaltiesServiceLink) shouldBe multiplePenaltiesBannerLinkText
            }

            "has the correct link destination" in {
              element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
            }

            "not display the penalties coming banner heading" in {
              elementExtinct(Selectors.penaltiesComingHeading)
            }

            "not display the penalties coming banner content" in {
              elementExtinct(Selectors.paraOne)
              elementExtinct(Selectors.paraTwo)
              elementExtinct(Selectors.paraThree)
            }

            "not display the penalties coming banner link" in {
              elementExtinct(Selectors.penaltiesComingLink)
            }
          }

        }

        "there are only estimated penalties" when {

          "there is one estimated penalty" should {

            lazy val view = injectedView(Some(model.copy(noOfEstimatedPenalties = 1, estimatedPenaltyAmount = 100.00)))
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct heading" in {
              elementText(Selectors.heading) shouldBe penaltiesBannerHeading
            }

            "have the correct penalty information" in {
              elementText(Selectors.penaltyInformation) shouldBe "Estimated penalty amount: £100"
            }

            "have a link to the penalties service" which {

              "has the correct text" in {
                elementText(Selectors.penaltiesServiceLink) shouldBe singlePenaltyBannerLinkText
              }

              "has the correct link destination" in {
                element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
              }
            }

            "not display the penalties coming banner heading" in {
              elementExtinct(Selectors.penaltiesComingHeading)
            }

            "not display the penalties coming banner content" in {
              elementExtinct(Selectors.paraOne)
              elementExtinct(Selectors.paraTwo)
              elementExtinct(Selectors.paraThree)
            }

            "not display the penalties coming banner link" in {
              elementExtinct(Selectors.penaltiesComingLink)
            }
          }

          "there is more than one estimated penalty" should {

            lazy val view = injectedView(Some(model.copy(noOfEstimatedPenalties = 2, estimatedPenaltyAmount = 200.99)))
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct heading" in {
              elementText(Selectors.heading) shouldBe penaltiesBannerHeading
            }

            "have the correct penalty information" in {
              elementText(Selectors.penaltyInformation) shouldBe "Estimated penalty amount: £200.99"
            }

            "have the correct link text" in {
              elementText(Selectors.penaltiesServiceLink) shouldBe multiplePenaltiesBannerLinkText
            }

            "has the correct link destination" in {
              element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
            }

            "not display the penalties coming banner heading" in {
              elementExtinct(Selectors.penaltiesComingHeading)
            }

            "not display the penalties coming banner content" in {
              elementExtinct(Selectors.paraOne)
              elementExtinct(Selectors.paraTwo)
              elementExtinct(Selectors.paraThree)
            }

            "not display the penalties coming banner link" in {
              elementExtinct(Selectors.penaltiesComingLink)
            }
          }
        }

        "there are both crystalised penalties and penalty points, but no estimated penalties" should {

          lazy val view = injectedView(Some(model.copy(noOfPoints = 1, noOfCrystalisedPenalties = 1, crystalisedPenaltyAmountDue = 100.00)))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe "Penalty amount to pay: £100 Total penalty points: 1"
          }

          "have the correct link text" in {
            elementText(Selectors.penaltiesServiceLink) shouldBe multiplePenaltiesBannerLinkText
          }

          "has the correct link destination" in {
            element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
          }

          "not display the penalties coming banner heading" in {
            elementExtinct(Selectors.penaltiesComingHeading)
          }

          "not display the penalties coming banner content" in {
            elementExtinct(Selectors.paraOne)
            elementExtinct(Selectors.paraTwo)
            elementExtinct(Selectors.paraThree)
          }

          "not display the penalties coming banner link" in {
            elementExtinct(Selectors.penaltiesComingLink)
          }
        }

        "there are both crystalised penalties and estimated penalties, but no penalty points" should {

          lazy val view = injectedView(Some(model.copy(noOfCrystalisedPenalties = 1, crystalisedPenaltyAmountDue = 100.00,
            noOfEstimatedPenalties = 1, estimatedPenaltyAmount = 150.00)))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe
              "Penalty amount to pay: £100 Estimated further penalty amount: £150"
          }

          "have the correct link text" in {
            elementText(Selectors.penaltiesServiceLink) shouldBe multiplePenaltiesBannerLinkText
          }

          "has the correct link destination" in {
            element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
          }

          "not display the penalties coming banner heading" in {
            elementExtinct(Selectors.penaltiesComingHeading)
          }

          "not display the penalties coming banner content" in {
            elementExtinct(Selectors.paraOne)
            elementExtinct(Selectors.paraTwo)
            elementExtinct(Selectors.paraThree)
          }

          "not display the penalties coming banner link" in {
            elementExtinct(Selectors.penaltiesComingLink)
          }
        }

        "there are both estimated penalties and penalty points, but no crystalised penalties" should {

          lazy val view = injectedView(Some(model.copy(noOfPoints = 1, noOfEstimatedPenalties = 1, estimatedPenaltyAmount = 150.00)))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe "Estimated penalty amount: £150 Total penalty points: 1"
          }

          "have the correct link text" in {
            elementText(Selectors.penaltiesServiceLink) shouldBe multiplePenaltiesBannerLinkText
          }

          "has the correct link destination" in {
            element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
          }

          "not display the penalties coming banner heading" in {
            elementExtinct(Selectors.penaltiesComingHeading)
          }

          "not display the penalties coming banner content" in {
            elementExtinct(Selectors.paraOne)
            elementExtinct(Selectors.paraTwo)
            elementExtinct(Selectors.paraThree)
          }

          "not display the penalties coming banner link" in {
            elementExtinct(Selectors.penaltiesComingLink)
          }
        }

        "there are crystalised penalties, estimated penalties and active points" should {

          lazy val view = injectedView(Some(model.copy(noOfPoints = 1, noOfCrystalisedPenalties = 1, crystalisedPenaltyAmountDue = 100.00,
            noOfEstimatedPenalties = 1, estimatedPenaltyAmount = 150.00)))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct heading" in {
            elementText(Selectors.heading) shouldBe penaltiesBannerHeading
          }

          "have the correct penalty information" in {
            elementText(Selectors.penaltyInformation) shouldBe
              "Penalty amount to pay: £100 Estimated further penalty amount: £150 Total penalty points: 1"
          }

          "have the correct link text" in {
            elementText(Selectors.penaltiesServiceLink) shouldBe multiplePenaltiesBannerLinkText
          }

          "has the correct link destination" in {
            element(Selectors.penaltiesServiceLink).attr("href") shouldBe mockConfig.penaltiesFrontendUrl
          }

          "not display the penalties coming banner heading" in {
            elementExtinct(Selectors.penaltiesComingHeading)
          }

          "not display the penalties coming banner content" in {
            elementExtinct(Selectors.paraOne)
            elementExtinct(Selectors.paraTwo)
            elementExtinct(Selectors.paraThree)
          }

          "not display the penalties coming banner link" in {
            elementExtinct(Selectors.penaltiesComingLink)
          }
        }
      }
    }
  }
}