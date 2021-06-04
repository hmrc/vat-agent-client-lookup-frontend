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

import assets.messages.{DirectDebitInterruptPageMessages => viewMessages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.agent.DirectDebitInterruptView

class DirectDebitInterruptViewSpec extends ViewBaseSpec {

  val injectedView: DirectDebitInterruptView = inject[DirectDebitInterruptView]

  "The DD interrupt screen for users" should {

    lazy val view = injectedView(request, messages, mockConfig)

    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"have the correct document title of '${viewMessages.title}'" in {
      document.title shouldBe (viewMessages.title + " - Your client’s VAT details - GOV.UK")
    }

    s"have the correct page heading of '${viewMessages.heading}'" in {
      elementText("h1") shouldBe viewMessages.heading
    }
  }

}
