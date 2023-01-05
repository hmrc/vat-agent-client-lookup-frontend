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

package forms

import models.{No, PreferenceModel, Yes, YesNo}
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.format.Formatter

object PreferenceForm {

  val email: String      = "email"
  val yesNo: String      = "yes_no"
  val yes: String        = "yes"
  val no: String         = "no"
  val maxLength: Int     = 138
  val mandatoryOptionError: String = "capturePref.error.mandatoryRadioOption"
  val mandatoryEmailError: String  = "capturePref.error.mandatoryEmail"
  val invalidEmailError: String    = "capturePref.error.invalidEmail"
  val emailLengthError: String     = "capturePref.error.emailLength"
  val emailRegex: String = """^(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\
    |x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[
    |a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4]
    |[0-9]|[01]?[0-9][0-9]?|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\
    |x09\x0b\x0c\x0e-\x7f])+)\])$""".stripMargin

  private def emailFormatter(key: String): String => Either[Seq[FormError], Option[String]] = {
    case x if  x.trim.length == 0         => Left(Seq(FormError(key, mandatoryEmailError)))
    case x if  x.trim.length > maxLength  => Left(Seq(FormError(key, emailLengthError)))
    case x if !x.trim.matches(emailRegex) => Left(Seq(FormError(key, invalidEmailError)))
    case x                                => Right(Some(x))
  }

  private val preferenceFormatter: Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      (data.get(yesNo), data.get(key)) match {
        case (Some(a), Some(x)) if a == yes => emailFormatter(key)(x)
        case (Some(a), None)    if a == yes => Left(Seq(FormError(key, mandatoryEmailError)))
        case _ => Right(None)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = {
      val stringValue = value match {
        case Some(x) => x.toString
        case _       => ""
      }

      Map(key -> stringValue)
    }
  }

  private val optionFormatter: Formatter[YesNo] = new Formatter[YesNo] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], YesNo] = {
      data.get(key) match {
        case Some(`yes`) => Right(Yes)
        case Some(`no`)  => Right(No)
        case _ => Left(Seq(FormError(key, mandatoryOptionError)))
      }
    }

    override def unbind(key: String, value: YesNo): Map[String, String] = {
      val stringValue = value match {
        case Yes => yes
        case No  => no
      }

      Map(key -> stringValue)
    }
  }

  val preferenceForm: Form[PreferenceModel] = Form(
    mapping(
      yesNo -> of(optionFormatter),
      email -> of(preferenceFormatter)
    )(PreferenceModel.apply)(PreferenceModel.unapply)
  )
}
