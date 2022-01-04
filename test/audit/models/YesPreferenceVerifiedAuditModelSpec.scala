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

package audit.models

import assets.BaseTestConstants.{arn, email}
import play.api.libs.json.Json
import utils.TestUtil

class YesPreferenceVerifiedAuditModelSpec extends TestUtil {

  "The YesPreferenceVerifiedAuditModel" should {

    val auditModel = YesPreferenceVerifiedAuditModel(arn, email)

    "have the correct transaction name" in {
      auditModel.transactionName shouldBe "agent-preference-email-address-verified"
    }

    "have the correct audit type" in {
      auditModel.auditType shouldBe "agentNotificationEmailAddressVerified"
    }

    "have the correct detail" in {
      val expectedJson = Json.obj(
        "agentReferenceNumber" -> arn,
        "notificationPreference" -> "Notification required",
        "verifiedNotificationEmailAddress" -> email
      )
      auditModel.detail shouldBe expectedJson
    }
  }
}
