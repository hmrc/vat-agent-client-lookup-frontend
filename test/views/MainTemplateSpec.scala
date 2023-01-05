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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.MainTemplate

class MainTemplateSpec extends ViewBaseSpec {

  val injectedView: MainTemplate = inject[MainTemplate]
  val headerSelector: String = ".hmrc-header__service-name"

  "The MainTemplate" should {

    lazy val view = injectedView("")(Html(""))
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct header title" in {
      elementText(headerSelector) shouldBe "Your client’s VAT details"
    }

    "have the correct header link" in {
      element(headerSelector).attr("href") shouldBe controllers.agent.routes.AgentHubController.show.url
    }
  }
}
