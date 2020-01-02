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

package models.agent

import play.api.libs.json._

sealed trait WhatToDoModel {
  val value: String
}

object WhatToDoModel {

  val id = "whatToDo"

  implicit val reads: Reads[WhatToDoModel] = (__ \ id).read[String].map {
    case SubmitReturn.value => SubmitReturn
    case ViewReturn.value => ViewReturn
    case ChangeDetails.value => ChangeDetails
    case ViewCertificate.value => ViewCertificate
  }

  implicit val writes: Writes[WhatToDoModel] = Writes {
    option => Json.obj(id -> option.value)
  }
}

object SubmitReturn extends WhatToDoModel {
  override val value = "submit-return"
}

object ViewReturn extends WhatToDoModel {
  override val value = "view-return"
}

object ChangeDetails extends WhatToDoModel {
  override val value = "change-details"
}

object ViewCertificate extends WhatToDoModel {
  override val value = "view-certificate"
}
