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

package views.agent.partials

import java.time.LocalDate
import messages.partials.{NextPaymentPartialMessages => Messages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.partials.NextPaymentPartial

class NextPaymentPartialSpec extends ViewBaseSpec {

  object Selectors {
    val tile = "#next-payment"
    val heading = "#next-payment-heading"
    val content = "#next-payment-paragraph"
    val link = "#what-you-owe-link"
    val ddLabel = ".govuk-tag"
    val overdueLabel = ".govuk-tag--red"
  }

  val nextPaymentPartial: NextPaymentPartial = inject[NextPaymentPartial]

  "The next payment partial" when {

    "there are no payments due and no DD information was retrieved" should {

      lazy val view = nextPaymentPartial(hybridUser = false, None, 0, isOverdue = false, isError = false, None)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct heading" in {
        elementText(Selectors.heading) shouldBe Messages.heading
      }

      "display the No payments due message" in {
        elementText(Selectors.content) shouldBe Messages.noPayments
      }

      "have the Check what you owe link" that {

        "has the correct text" in {
          elementText(Selectors.link) shouldBe Messages.linkText
        }

        "has the correct href" in {
          element(Selectors.link).attr("href") shouldBe mockConfig.whatYouOweUrl
        }
      }

      "not display a DD label" in {
        elementExtinct(Selectors.ddLabel)
      }

      "not display the overdue flag" in {
        elementExtinct(Selectors.overdueLabel)
      }
    }

    "there is one payment and it is not overdue" should {

      lazy val view = nextPaymentPartial(hybridUser = false, Some(LocalDate.parse("2018-01-01")), 1, isOverdue = false, isError = false, None)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the date of the payment correctly" in {
        elementText(Selectors.content) shouldBe Messages.date
      }

      "display the date of the payment correctly using non breaking space" in {
        elementText(Selectors.content).contains(Messages.dateNonBreakingSpace)
      }

      "not display the overdue flag" in {
        elementExtinct(Selectors.overdueLabel)
      }
    }

    "there is one payment that is overdue" should {

      lazy val view = nextPaymentPartial(hybridUser = false, Some(LocalDate.parse("2018-01-01")), 1, isOverdue = true, isError = false, None)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the overdue label next to the payment date" in {
        elementText(Selectors.content) should include(Messages.overdue)
      }
    }

    "there is more than one payment" should {

      lazy val view = nextPaymentPartial(hybridUser = false, Some(LocalDate.parse("2018-01-01")), 10, isOverdue = false, isError = false, None)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct title" in {
        elementText(Selectors.heading) shouldBe Messages.headingPayments
      }

      "display the message with the correct number of payments" in {
        elementText(Selectors.content) shouldBe Messages.payments
      }

      "not display the overdue flag" in {
        elementExtinct(Selectors.overdueLabel)
      }
    }

    "there is no payments returned due to Financial API failure" should {

      lazy val view = nextPaymentPartial(hybridUser = false, Some(LocalDate.parse("2018-01-01")), 1, isOverdue = false, isError = true, None)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct title" in {
        elementText(Selectors.heading) shouldBe Messages.heading
      }

      "display the error message" in {
        elementText(Selectors.content) shouldBe Messages.errorMessage
      }
    }

    "the user is hybrid" should {

      lazy val view = nextPaymentPartial(hybridUser = true, None, 0, isOverdue = false, isError = false, None)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not exist" in {
        elementExtinct(Selectors.tile)
      }
    }

    "the client has a direct debit set up" should {

      lazy val view = nextPaymentPartial(hybridUser = false, Some(LocalDate.parse("2018-01-01")), 1, isOverdue = false, isError = false, Some(true))
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the DD set up label" which {

        "has the correct content" in {
          elementText(Selectors.ddLabel) shouldBe Messages.ddSetUp
        }

        "has the correct label colour" in {
          element(Selectors.ddLabel).hasClass("govuk-tag--green")
        }
      }
    }

    "the client does not have a direct debit set up" should {

      lazy val view = nextPaymentPartial(hybridUser = false, Some(LocalDate.parse("2018-01-01")), 1, isOverdue = false, isError = false, Some(false))
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the DD is not set up label" which {

        "has the correct content" in {
          elementText(Selectors.ddLabel) shouldBe Messages.ddNotSetUp
        }

        "has the correct label colour" in {
          element(Selectors.ddLabel).hasClass("govuk-tag--red")
        }
      }
    }
  }
}
