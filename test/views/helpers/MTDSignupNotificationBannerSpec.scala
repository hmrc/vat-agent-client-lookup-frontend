/*
 * Copyright 2022 HM Revenue & Customs
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

package views.helpers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.helpers.MTDSignupNotificationBanner

class MTDSignupNotificationBannerSpec extends ViewBaseSpec {

  val mtdSignupNotificationBanner: MTDSignupNotificationBanner = inject[MTDSignupNotificationBanner]

  object Selectors {
    val signUpBanner = ".govuk-notification-banner"
    val bannerTitle = ".govuk-notification-banner__title"
    val bannerHeading = "#mtd-sign-up-banner-heading"
    val bannerText = ".govuk-body"
    val bannerLink = "a"
  }

  "The MTDSignUpBanner" when {

    "the user has a mandation status of 'Non MTDfB'" should {

      lazy val banner = mtdSignupNotificationBanner("Non MTDfB")
      lazy implicit val document: Document = Jsoup.parse(banner.body)

      "display the banner" in {
        element(Selectors.signUpBanner)
      }

      "have the correct banner title" in {
        elementText(Selectors.bannerTitle) shouldBe "Important"
      }

      "have the correct banner heading" in {
        elementText(Selectors.bannerHeading) shouldBe "The way to submit VAT returns changed on 1 April due to Making Tax Digital"
      }

      "have the correct banner text" in {
        elementText(Selectors.bannerText) shouldBe "You cannot use this service to submit returns for accounting periods " +
          "starting after 1 April 2022. Instead, digital records must be kept and returns must be submitted using HMRC compatible " +
          "software. Find out when to sign up and start using Making Tax Digital for VAT (opens in a new tab)."
      }

      "have the correct link text" in {
        elementText(Selectors.bannerLink) shouldBe "Find out when to sign up and start using Making Tax Digital for VAT (opens in a new tab)"
      }

      "has the correct href for the link" in {
        element(Selectors.bannerLink).attr("href") shouldBe "https://www.gov.uk/guidance/when-to-start-using-making-tax-digital-for-vat-if-youve-not-before"
      }
    }

    "the user has a different mandation status than 'Non MTDfB'" should {

      lazy val banner = mtdSignupNotificationBanner("Non Digital")
      lazy implicit val document: Document = Jsoup.parse(banner.body)

      "not display the banner" in {
        elementExtinct(Selectors.signUpBanner)
      }
    }
  }
}
