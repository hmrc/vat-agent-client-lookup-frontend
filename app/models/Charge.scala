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

package models

import play.api.libs.json.{JsPath, Reads}
import java.time.LocalDate

import play.api.libs.functional.syntax.{toAlternativeOps, toFunctionalBuilderOps}

case class Charge(chargeType: String,
                  outstandingAmount: BigDecimal,
                  dueDate: LocalDate,
                  ddCollectionInProgress: Boolean)

object Charge {

  implicit val reads: Reads[Charge] = (
    (JsPath \ "chargeType").read[String] and
    (JsPath \ "outstandingAmount").read[BigDecimal] and
    (JsPath \ "items")(0).\("dueDate").read[LocalDate] and
    (JsPath \ "items")(0).\("DDcollectionInProgress").read[Boolean].or(Reads.pure(false))
  ) (Charge.apply _)

}
