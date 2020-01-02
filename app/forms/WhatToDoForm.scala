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

package forms

import models.agent._
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.format.Formatter

object WhatToDoForm {

  val option: String = "option"
  val submitReturn = "submit-return"
  val viewReturn = "view-return"
  val changeDetails = "change-details"
  val viewCertificate = "view-certificate"

  val error: String = "whatToDo.error.mandatoryRadioOption"

  private val formatter: Formatter[WhatToDoModel] = new Formatter[WhatToDoModel] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], WhatToDoModel] = {
      data.get(key) match {
        case Some(`submitReturn`) => Right(SubmitReturn)
        case Some(`viewReturn`) => Right(ViewReturn)
        case Some(`changeDetails`) => Right(ChangeDetails)
        case Some(`viewCertificate`) => Right(ViewCertificate)
        case _ => Left(Seq(FormError(key, error)))
      }
    }

    override def unbind(key: String, value: WhatToDoModel): Map[String, String] = {
      val stringValue = value match {
        case SubmitReturn => submitReturn
        case ViewReturn => viewReturn
        case ChangeDetails => changeDetails
        case ViewCertificate => viewCertificate
      }
      Map(key -> stringValue)
    }
  }

  val whatToDoForm: Form[WhatToDoModel] = Form(
    single(
      option -> of(formatter)
    )
  )

}
