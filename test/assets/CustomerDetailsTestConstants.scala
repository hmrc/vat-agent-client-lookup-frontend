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

package assets

import java.time.LocalDate

import models.{CustomerDetails, Deregistration, ChangeIndicators}
import play.api.libs.json.{JsObject, Json}

object CustomerDetailsTestConstants {

  val orgName = "Ancient Antiques Ltd"
  val tradingName = "Dusty Relics"
  val firstName = "Fred"
  val lastName = "Flintstone"
  val mandationStatus = "MTDfB Mandated"
  val nonMTDfB = "Non MTDfB"
  val nonDigital = "Non Digital"
  val vatGroup = "Z2"
  val validParty = "2"
  val missingTrader = true

  val noOptionalInfoJson: JsObject = Json.obj(
    "mandationStatus" -> mandationStatus,
    "isInsolvent" -> false,
    "missingTrader" -> missingTrader
  )

  val allInfoJson: JsObject = Json.obj(
    "firstName" -> firstName,
    "lastName" -> lastName,
    "organisationName" -> orgName,
    "tradingName" -> tradingName,
    "partyType" -> "2",
    "mandationStatus" -> mandationStatus,
    "isInsolvent" -> false,
    "deregistration" -> Json.obj(
      "effectDateOfCancellation" -> "2019-01-01"
    ),
    "missingTrader" -> true
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
    isInsolvent = false
  )

  val customerDetailsNoInfoVatGroup: CustomerDetails = CustomerDetails(
    None,
    None,
    None,
    None,
    Some(vatGroup),
    mandationStatus,
    None,
    isInsolvent = false
  )

  val customerDetailsFnameOnly: CustomerDetails = CustomerDetails(
    Some(firstName),
    None,
    None,
    None,
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false
  )

  val customerDetailsLnameOnly: CustomerDetails = CustomerDetails(
    None,
    Some(lastName),
    None,
    None,
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false
  )

  val customerDetailsNoTradingName: CustomerDetails = CustomerDetails(
    None,
    None,
    Some(orgName),
    None,
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false
  )

  val customerDetailsIndividual: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    None,
    None,
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false
  )

  val customerDetailsOrganisation: CustomerDetails = CustomerDetails(
    None,
    None,
    Some(orgName),
    Some(tradingName),
    Some(validParty),
    mandationStatus,
    None,
    isInsolvent = false
  )

  val customerDetailsNonDigital: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(validParty),
    nonDigital,
    None,
    isInsolvent = false
  )

  val customerDetailsOptedOut: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(validParty),
    nonMTDfB,
    Some(Deregistration(Some(LocalDate.parse("2019-01-01")))),
    isInsolvent = false
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
    isInsolvent = false
  )

  val customerDetailsFutureDeregisterOptedOutVatGroup: CustomerDetails = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(vatGroup),
    nonMTDfB,
    Some(Deregistration(Some(LocalDate.parse("2020-01-01")))),
    isInsolvent = false
  )

  val clientNoPartyType: CustomerDetails = CustomerDetails(
    None,
    None,
    None,
    None,
    None,
    mandationStatus,
    None,
    isInsolvent = false
  )

  val customerDetailsInsolvent: CustomerDetails = customerDetailsAllInfo.copy(isInsolvent = true)
}
