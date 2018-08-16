/*
 * Copyright 2018 HM Revenue & Customs
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

package testOnly.controllers

import controllers.ControllerBaseSpec
import play.api.http.Status
import play.api.test.FakeRequest

class JourneySetupControllerSpec extends ControllerBaseSpec {

  private lazy val controller = new JourneySetupController(messagesApi, mockAppConfig, http)

  "Calling the journeySetup action" should {

    lazy val request = FakeRequest("GET", "/")
    lazy val result = controller.journeySetup()(request)

    "return a 200" in {
      status(result) shouldBe Status.OK
    }

    "result in a redirectUrl being stored in session to access manage-vat-subscription-frontend" in {

        val sessionUrl = await(result map {

          res => {
            res.session.get(common.SessionKeys.redirectUrl).fold("")(_.self)
          }

        })

      sessionUrl shouldEqual mockAppConfig.manageVatBase
    }

  }

}
