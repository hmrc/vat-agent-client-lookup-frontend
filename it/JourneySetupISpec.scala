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

import java.net.URLDecoder

import common.SessionKeys
import helpers.SessionCookieCrumbler
import pages.BasePageISpec
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.SEE_OTHER

class JourneySetupISpec extends BasePageISpec {

  val path = "/journey-setup"
  val redirectUrl = "http://localhost:9149/homepage"

  "Calling the .journeySetup() action" when {

    def journeySetup(): WSResponse = postJSValueBody(path)(Json.obj(SessionKeys.redirectUrl -> redirectUrl))

    "a redirect URL is provided in the post" should {

      "redirect to the Select Client VRN page and have the redirect URL in the session" in {

        given.agent.isSignedUpToAgentServices

        val res = journeySetup()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(controllers.agent.routes.SelectClientVrnController.show().url)
        )

        val urlInSession = SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.redirectUrl)
        URLDecoder.decode(urlInSession.getOrElse(""), "UTF-8") shouldBe redirectUrl
      }
    }
  }
}
