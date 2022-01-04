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

package controllers

import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class SignOutControllerSpec extends ControllerBaseSpec {


  object TestSignOutController extends SignOutController(mcc, mockConfig)

  "Navigating to sign out page" when {

    "feedback on sign out is enabled" should {

      val result: Future[Result] = TestSignOutController.signOut(feedbackOnSignOut = true)(request)

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the survey page" in {
        redirectLocation(result) shouldBe Some(mockConfig.feedbackSignOutUrl)
      }
    }

    "feedback on sign out is disabled" should {

      val result: Future[Result] = TestSignOutController.signOut(feedbackOnSignOut = false)(request)

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the session timeout page" in {
        redirectLocation(result) shouldBe Some(mockConfig.unauthorisedSignOutUrl)
      }
    }
  }
}
