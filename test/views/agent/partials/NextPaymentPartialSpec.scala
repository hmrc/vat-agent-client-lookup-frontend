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
  }

  val nextPaymentPartial: NextPaymentPartial = inject[NextPaymentPartial]

  "The next payment partial" when {

    "there are no payments due" should {

      lazy val view = nextPaymentPartial(hybridUser = false, None, 0, isOverdue = false)(messages, mockConfig)
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
    }

    "there is one payment and it is not overdue" should {

      lazy val view = nextPaymentPartial(hybridUser = false, Some(LocalDate.parse("2018-01-01")), 1, isOverdue = false)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the date of the payment correctly" in {
        elementText(Selectors.content) shouldBe Messages.date
      }
    }

    "there is one payment that is overdue" should {

      lazy val view = nextPaymentPartial(hybridUser = false, Some(LocalDate.parse("2018-01-01")), 1, isOverdue = true)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the overdue label next to the payment date" in {
        elementText(Selectors.content) should include(Messages.overdue)
      }
    }

    "there is more than one payment" should {

      lazy val view = nextPaymentPartial(hybridUser = false, Some(LocalDate.parse("2018-01-01")), 10, isOverdue = false)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct title" in {
        elementText(Selectors.heading) shouldBe Messages.headingPayments
      }

      "display the message with the correct number of payments" in {
        elementText(Selectors.content) shouldBe Messages.payments
      }
    }

    "the user is hybrid" should {

      lazy val view = nextPaymentPartial(hybridUser = true, Some(LocalDate.parse("2018-01-01")), 10, isOverdue = false)(messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not exist" in {
        elementExtinct(Selectors.tile)
      }
    }
  }

}
