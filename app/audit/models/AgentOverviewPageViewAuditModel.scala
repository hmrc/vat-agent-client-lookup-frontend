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

import models.{User, VatDetailsDataModel}
import play.api.libs.json.{JsObject, JsValue, Json}

case class AgentOverviewPageViewAuditModel(user: User[_],
                                           vatDetailsModel: VatDetailsDataModel) extends ExtendedAuditModel {

  private val paymentDetails: JsObject = vatDetailsModel match {
    case VatDetailsDataModel(charges, false) if charges.size > 1 =>
      Json.obj("numberOfPayments" -> charges.size.toString, "paymentOutstanding" -> "yes")
    case VatDetailsDataModel(charges, false) if charges.size == 1 =>
      Json.obj("paymentDueBy" -> charges.head.dueDate.toString, "paymentOutstanding" -> "yes")
    case VatDetailsDataModel(_, false) =>
      Json.obj("paymentOutstanding" -> "no")
    case VatDetailsDataModel(_, true) =>
      Json.obj("paymentsTileError" -> "true")
  }

  override val transactionName: String = "view-next-outstanding-vat-payment"
  override val auditType: String = "AgentOverviewPageView"
  override val detail: JsValue = paymentDetails ++ Json.obj(
    "agentReferenceNumber" -> user.arn,
    "vrn" -> user.vrn
  )
}
