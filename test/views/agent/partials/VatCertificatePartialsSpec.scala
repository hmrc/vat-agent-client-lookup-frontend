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

package views.agent.partials

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec
import assets.messages.partials.VatCertificatePartialMessages
import views.html.agent.partials.VatCertificatePartials

class VatCertificatePartialsSpec extends ViewBaseSpec {

  val vatCertificatePartials: VatCertificatePartials = injector.instanceOf[VatCertificatePartials]

  "ClientDetailsPartials" should {

    lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
    lazy val view = vatCertificatePartials()(messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"have the correct card heading as ${VatCertificatePartialMessages.heading}" in {
      elementText(".heading-medium") shouldBe VatCertificatePartialMessages.heading
    }

    s"display the correct line 1 as ${VatCertificatePartialMessages.paragraphOne}" in {
      elementText("p") shouldBe VatCertificatePartialMessages.paragraphOne
    }

    s"display the correct link text as ${VatCertificatePartialMessages.linkText}" in {
      elementText("a") shouldBe VatCertificatePartialMessages.linkText
    }

    s"display the correct link of ${mockConfig.vatCertificateUrl}" in {
      element("a").attr("href") shouldBe mockConfig.vatCertificateUrl
    }
  }

}
