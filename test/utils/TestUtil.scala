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

package utils

import assets.BaseTestConstants._
import common.SessionKeys
import common.MandationStatus.nonMTDfB
import config.ErrorHandler
import mocks.MockAppConfig
import models.{Agent, User}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext

trait TestUtil extends UnitSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with Injecting {

  override def beforeEach(): Unit = {
    mockConfig.features.useStaticDateFeature(true)
  }

  lazy val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  lazy val messagesApi: MessagesApi = inject[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(Lang("en-GB"), messagesApi)

  implicit lazy val mockConfig: MockAppConfig = new MockAppConfig(app.configuration)
  implicit lazy val serviceErrorHandler: ErrorHandler = inject[ErrorHandler]

  lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  lazy val language: Lang = mockConfig.languageMap("english")


  lazy val fakeRequestWithVrnAndRedirectUrl: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(
      SessionKeys.clientVRN -> vrn,
      SessionKeys.redirectUrl -> "/homepage"
    )

  lazy val fakeRequestWithMtdVatAgentData: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(
      SessionKeys.clientVRN -> vrn,
      SessionKeys.redirectUrl -> "/homepage",
      SessionKeys.mtdVatAgentClientName -> "l'biz",
      SessionKeys.mtdVatAgentMandationStatus -> nonMTDfB
    )

  lazy val user: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(request)
  lazy val agent: Agent[AnyContentAsEmpty.type] = Agent[AnyContentAsEmpty.type](arn)(fakeRequestWithVrnAndRedirectUrl)
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

}
