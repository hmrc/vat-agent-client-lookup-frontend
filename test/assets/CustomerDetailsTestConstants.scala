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

package assets

import java.time.LocalDate

import models.{CustomerDetails, Deregistration, ChangeIndicators}
import play.api.libs.json.{JsObject, Json}

object CustomerDetailsTestConstants {

  val orgName = "Ancient Antiques Ltd"
  val tradingName = "Dusty Relics"
  val firstName = "Fred"
  val lastName = "Flintstone"
  val userName = "Fred Flintstone"
  val mandationStatus = "MTDfB Mandated"
  val nonMTDfB = "Non MTDfB"
  val nonDigital = "Non Digital"
  val vatGroup = "Z2"
  val validParty = "2"
  val missingTrader = true
  val isPartialMigration = false
  val migratedToETMPDate = "2018-03-03"

  val noOptionalInfoJson: JsObject = Json.obj(
    "customerDetails" -> Json.obj(
      "isInsolvent" -> false,
      "isPartialMigration" -> isPartialMigration
    ),
    "mandationStatus" -> mandationStatus,
    "missingTrader" -> missingTrader
  )

  val allInfoJson: JsObject = Json.obj(
    "customerDetails" -> Json.obj(
      "firstName" -> firstName,
      "lastName" -> lastName,
      "organisationName" -> orgName,
      "tradingName" -> tradingName,
      "isInsolvent" -> false,
      "isPartialMigration" -> isPartialMigration,
      "customerMigratedToETMPDate" -> "2018-03-03"
    ),
    "partyType" -> "2",
    "mandationStatus" -> mandationStatus,
    "deregistration" -> Json.obj(
      "effectDateOfCancellation" -> "2019-01-01"
    ),
    "missingTrader" -> true
  )

  val pendingInfoJson: JsObject = Json.obj(
    "customerDetails" -> Json.obj(
      "firstName" -> firstName,
      "lastName" -> lastName,
      "organisationName" -> orgName,
      "tradingName" -> tradingName,
      "isInsolvent" -> false,
      "isPartialMigration" -> isPartialMigration,
      "customerMigratedToETMPDate" -> "2018-03-03"
    ),
    "partyType" -> "2",
    "mandationStatus" -> mandationStatus,
    "deregistration" -> Json.obj(
      "effectDateOfCancellation" -> "2019-01-01"
    ),
    "missingTrader" -> true,
    "pendingChanges" -> Json.obj(
      "organisationName" -> s"New $orgName",
      "tradingName" -> s"New $tradingName"
    )
  )

  val customerDetailsAllInfo: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(validParty),
    mandationStatus,
    Some(Deregistration(Some(LocalDate.parse("2019-01-01")))),
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    Some(migratedToETMPDate),
    None,
    missingTrader = missingTrader
  )

  val customerDetailsAllInfoVatGroup: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(vatGroup),
    mandationStatus,
    Some(Deregistration(Some(LocalDate.parse("2019-01-01")))),
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    Some(migratedToETMPDate),
    None,
    missingTrader
  )

  val customerDetailsNoInfo: CustomerDetails = CustomerDetails(
    None,
    None,
    None,
    None,
    None,
    mandationStatus,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    None,
    None,
    missingTrader
  )

  val customerDetailsNoInfoWithPartyType: CustomerDetails = CustomerDetails(
    None,
    None,
    None,
    None,
    Some("2"),
    mandationStatus,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    None
  )

  val customerDetailsNoInfoVatGroup: CustomerDetails = CustomerDetails(
    None,
    None,
    None,
    None,
    Some(vatGroup),
    mandationStatus,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    None
  )

  val customerDetailsFnameOnly: CustomerDetails = CustomerDetails(
    Some(firstName),
    None,
    None,
    None,
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    None
  )

  val customerDetailsLnameOnly: CustomerDetails = CustomerDetails(
    None,
    Some(lastName),
    None,
    None,
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    None
  )

  val customerDetailsNoTradingName: CustomerDetails = CustomerDetails(
    None,
    None,
    Some(orgName),
    None,
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    None
  )

  val customerDetailsIndividual: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    None,
    None,
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    Some(migratedToETMPDate)
  )

  val customerDetailsOrganisation: CustomerDetails = CustomerDetails(
    None,
    None,
    Some(orgName),
    Some(tradingName),
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    Some(migratedToETMPDate)
  )

  val customerDetailsNonDigital: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(validParty),
    nonDigital,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    Some(migratedToETMPDate)
  )

  val customerDetailsOptedOut: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(validParty),
    nonMTDfB,
    Some(Deregistration(Some(LocalDate.parse("2019-01-01")))),
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    Some(migratedToETMPDate)
  )

  val customerDetailsAllPending: CustomerDetails = customerDetailsAllInfo.copy(
    deregistration = None,
    changeIndicators = Some(ChangeIndicators(deregister = true, PPOBDetails = true))
  )

  val customerDetailsFutureDeregisterOptedOut: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(validParty),
    nonMTDfB,
    Some(Deregistration(Some(LocalDate.parse("2020-01-01")))),
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    Some(migratedToETMPDate)
  )

  val customerDetailsFutureDeregisterOptedOutVatGroup: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(vatGroup),
    nonMTDfB,
    Some(Deregistration(Some(LocalDate.parse("2020-01-01")))),
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    Some(migratedToETMPDate)
  )

  val clientNoPartyType: CustomerDetails = CustomerDetails(
    None,
    None,
    None,
    None,
    None,
    mandationStatus,
    None,
    isInsolvent = false,
    isHybridUser = isPartialMigration,
    None
  )

  val customerDetailsInsolvent: CustomerDetails = customerDetailsAllInfo.copy(isInsolvent = true)
  val customerDetailsHybrid: CustomerDetails = customerDetailsAllInfo.copy(isHybridUser = true, missingTrader = false)
}
