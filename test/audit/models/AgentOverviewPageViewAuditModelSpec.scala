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

package audit.models

import assets.BaseTestConstants.{arn, vrn}
import assets.FinancialDataConstants.{onePaymentModelOverdue, paymentsModelNoPayments, paymentsModelNoneOverdue}
import models.User
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class AgentOverviewPageViewAuditModelSpec extends AnyWordSpecLike with Matchers {

  lazy val user: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true, Some(arn))(FakeRequest())

  val noPaymentsAuditModel: AgentOverviewPageViewAuditModel =
    AgentOverviewPageViewAuditModel(user, paymentsModelNoPayments)

  "AgentOverviewPageViewAuditModel" should {

    "have the correct transaction name" in {
      noPaymentsAuditModel.transactionName shouldBe "view-next-outstanding-vat-payment"
    }

    "have the correct audit type" in {
      noPaymentsAuditModel.auditType shouldBe "AgentOverviewPageView"
    }

    "have the correct detail" when {

      "there is one outstanding payment" in {
        val auditModel: AgentOverviewPageViewAuditModel = AgentOverviewPageViewAuditModel(user, onePaymentModelOverdue)

        val expected: JsObject = Json.obj(
          "agentReferenceNumber" -> arn,
          "vrn" -> vrn,
          "paymentDueBy" -> "2018-01-01",
          "paymentOutstanding" -> "yes"
        )

        auditModel.detail shouldBe expected
      }

      "there are multiple outstanding payments" in {

        val auditModel: AgentOverviewPageViewAuditModel = AgentOverviewPageViewAuditModel(user, paymentsModelNoneOverdue)

        val expected: JsObject = Json.obj(
          "agentReferenceNumber" -> arn,
          "vrn" -> vrn,
          "numberOfPayments" -> "2",
          "paymentOutstanding" -> "yes"
        )

        auditModel.detail shouldBe expected
      }

      "there are no outstanding payments" in {

        val expected: JsObject = Json.obj(
          "agentReferenceNumber" -> arn,
          "vrn" -> vrn,
          "paymentOutstanding" -> "no"
        )

        noPaymentsAuditModel.detail shouldBe expected
      }

      "there was an error receiving financial data" in {
        val auditModel: AgentOverviewPageViewAuditModel =
          AgentOverviewPageViewAuditModel(user, paymentsModelNoPayments.copy(isError = true))

        val expected: JsObject = Json.obj(
          "agentReferenceNumber" -> arn,
          "vrn" -> vrn,
          "paymentsTileError" -> "true"
        )

        auditModel.detail shouldBe expected
      }
    }
  }
}
