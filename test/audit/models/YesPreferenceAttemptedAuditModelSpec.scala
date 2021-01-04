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

package audit.models

import assets.BaseTestConstants._
import play.api.libs.json.Json
import utils.TestUtil

class YesPreferenceAttemptedAuditModelSpec extends TestUtil {

  val transactionName = "agent-preference-email-address-attempted"
  val auditEvent = "agentNotificationEmailAddressAttempted"

  "The YesPreferenceAttemptedAuditModel" should {

    lazy val testYesPreferenceAuditModel = YesPreferenceAttemptedAuditModel(arn, email)

    s"have the correct transaction name of '$transactionName'" in {
      testYesPreferenceAuditModel.transactionName shouldBe transactionName
    }

    s"have the correct audit event type of '$auditEvent'" in {
      testYesPreferenceAuditModel.auditType shouldBe auditEvent
    }

    "have the correct details for the audit event" in {
      testYesPreferenceAuditModel.detail shouldBe Json.obj(
        "agentReferenceNumber" -> arn,
        "notificationPreference" -> "Notification required",
        "attemptedNotificationEmailAddress" -> email
      )
    }
  }
}
