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

package models

import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException

case class CustomerDetails(firstName: Option[String],
                           lastName: Option[String],
                           organisationName: Option[String],
                           tradingName: Option[String],
                           partyType: Option[String],
                           mandationStatus: String,
                           deregistration: Option[Deregistration],
                           isInsolvent: Boolean,
                           customerMigratedToETMPDate: Option[String],
                           changeIndicators: Option[ChangeIndicators] = None,
                           missingTrader: Boolean = false) {

  val userName: Option[String] = {
    val name = s"${firstName.getOrElse("")} ${lastName.getOrElse("")}".trim
    if (name.isEmpty) None else Some(name)
  }

  lazy val clientName: String = (tradingName, organisationName, userName) match {
    case (Some(tName), _, _) => tName
    case (_, Some(oName), _) => oName
    case (_, _, Some(uName)) => uName
    case (_, _, _) =>
      Logger.warn("[CustomerDetails][clientName] - No entity name was returned by the API")
      throw new InternalServerException("No entity name was returned by the API")
  }

  val hasPendingPPOB: Boolean = changeIndicators.fold(false)(_.PPOBDetails)
}

object CustomerDetails {

  private val firstNamePath = __ \ "customerDetails" \ "firstName"
  private val lastNamePath = __ \ "customerDetails" \ "lastName"
  private val organisationNamePath = __ \ "customerDetails" \ "organisationName"
  private val tradingNamePath = __ \ "customerDetails" \ "tradingName"
  private val partyTypePath = __ \ "partyType"
  private val mandationStatusPath = __ \ "mandationStatus"
  private val deregistrationPath = __ \ "deregistration"
  private val isInsolventPath = __ \ "customerDetails" \ "isInsolvent"
  private val changeIndicatorsPath = __ \ "changeIndicators"
  private val missingTraderPath = __ \ "missingTrader"
  private val migratedToETMPDatePath = __ \ "customerDetails" \ "customerMigratedToETMPDate"

  implicit val reads: Reads[CustomerDetails] = (
    firstNamePath.readNullable[String] and
    lastNamePath.readNullable[String] and
    organisationNamePath.readNullable[String] and
    tradingNamePath.readNullable[String] and
    partyTypePath.readNullable[String] and
    mandationStatusPath.read[String] and
    deregistrationPath.readNullable[Deregistration] and
    isInsolventPath.read[Boolean] and
    migratedToETMPDatePath.readNullable[String] and
    changeIndicatorsPath.readNullable[ChangeIndicators] and
    missingTraderPath.read[Boolean]
  ) (CustomerDetails.apply _)
}
