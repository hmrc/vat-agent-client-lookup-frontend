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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import assets.messages.partials.VatCertificatePartialMessages
import views.html.agent.partials.VatCertificatePartial

class VatCertificatePartialSpec extends ViewBaseSpec {

  val vatCertificatePartials: VatCertificatePartial = inject[VatCertificatePartial]

  "VatCertificatePartial" should {

    lazy val view = vatCertificatePartials()(messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"display the correct link text as ${VatCertificatePartialMessages.linkText}" in {
      elementText("#certificate-link") shouldBe VatCertificatePartialMessages.linkText
    }

    s"display the correct link of ${mockConfig.vatCertificateUrl}" in {
      element("#certificate-link").attr("href") shouldBe mockConfig.vatCertificateUrl
    }

    s"display the correct line 1 as ${VatCertificatePartialMessages.paragraphOne}" in {
      elementText("#certificate-body") shouldBe VatCertificatePartialMessages.paragraphOne
    }

  }

}
