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

import common.MandationStatus
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException
import utils.LoggingUtil

case class CustomerDetails(firstName: Option[String],
                           lastName: Option[String],
                           organisationName: Option[String],
                           tradingName: Option[String],
                           partyType: Option[String],
                           mandationStatus: String,
                           deregistration: Option[Deregistration],
                           isInsolvent: Boolean,
                           continueToTrade: Option[Boolean],
                           insolvencyType: Option[String],
                           isHybridUser: Boolean,
                           customerMigratedToETMPDate: Option[String],
                           changeIndicators: Option[ChangeIndicators] = None,
                           missingTrader: Boolean = false) extends LoggingUtil {

  val userName: Option[String] = {
    val name = s"${firstName.getOrElse("")} ${lastName.getOrElse("")}".trim
    if (name.isEmpty) None else Some(name)
  }

  lazy val optedIn: Boolean = {
    !List(MandationStatus.nonMTDfB, MandationStatus.nonDigital, MandationStatus.MTDfBExempt).contains(mandationStatus)
  }

  lazy val clientName: String = (tradingName, organisationName, userName) match {
    case (Some(tName), _, _) => tName
    case (_, Some(oName), _) => oName
    case (_, _, Some(uName)) => uName
    case (_, _, _) =>
      logger.error("[CustomerDetails][clientName] - No entity name was returned by the API")
      throw new InternalServerException("No entity name was returned by the API")
  }

  val hasPendingPPOB: Boolean = changeIndicators.fold(false)(_.PPOBDetails)

  val exemptInsolvencyTypes = Seq("07", "12", "14")
  val blockedInsolvencyTypes = Seq("01", "02", "03", "06", "08", "09", "10", "13", "15")

  val isInsolventWithoutAccess: Boolean = (isInsolvent, insolvencyType) match {
    case (true, Some(inType)) if exemptInsolvencyTypes.contains(inType) => false
    case (true, Some(inType)) if blockedInsolvencyTypes.contains(inType) => true
    case (true, _) if continueToTrade.contains(false) => true
    case _ => false
  }
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
  private val continueToTradePath = __ \ "customerDetails" \ "continueToTrade"
  private val insolvencyTypePath = __ \ "customerDetails" \ "insolvencyType"
  private val changeIndicatorsPath = __ \ "changeIndicators"
  private val missingTraderPath = __ \ "missingTrader"
  private val isPartialMigrationPath = __ \ "customerDetails"\ "isPartialMigration"
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
    continueToTradePath.readNullable[Boolean] and
    insolvencyTypePath.readNullable[String] and
    isPartialMigrationPath.read[Boolean] and
    migratedToETMPDatePath.readNullable[String] and
    changeIndicatorsPath.readNullable[ChangeIndicators] and
    missingTraderPath.read[Boolean]
  ) (CustomerDetails.apply _)
}
