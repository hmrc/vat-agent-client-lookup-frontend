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

  val noOptionalInfoJson: JsObject = Json.obj(
    "mandationStatus" -> mandationStatus
  )

  val allInfoJson: JsObject = Json.obj(
    "firstName" -> firstName,
    "lastName" -> lastName,
    "organisationName" -> orgName,
    "tradingName" -> tradingName,
    "mandationStatus" -> mandationStatus,
    "deregistration" -> Json.obj(
      "effectDateOfCancellation" -> "2019-01-01"
    )
  )

  val customerDetailsAllInfo = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    None,
    mandationStatus,
    Some(Deregistration(Some(LocalDate.parse("2019-01-01"))))
  )

  val customerDetailsAllInfoVatGroup = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(vatGroup),
    mandationStatus,
    Some(Deregistration(Some(LocalDate.parse("2019-01-01"))))
  )

  val customerDetailsNoInfo = CustomerDetails(
    None,
    None,
    None,
    None,
    None,
    mandationStatus,
    None
  )

  val customerDetailsNoInfoVatGroup = CustomerDetails(
    None,
    None,
    None,
    None,
    Some(vatGroup),
    mandationStatus,
    None
  )

  val customerDetailsFnameOnly = CustomerDetails(
    Some(firstName),
    None,
    None,
    None,
    None,
    mandationStatus,
    None
  )

  val customerDetailsLnameOnly = CustomerDetails(
    None,
    Some(lastName),
    None,
    None,
    None,
    mandationStatus,
    None
  )

  val customerDetailsNoTradingName = CustomerDetails(
    None,
    None,
    Some(orgName),
    None,
    None,
    mandationStatus,
    None
  )

  val customerDetailsIndividual = CustomerDetails(
    Some(firstName),
    Some(lastName),
    None,
    None,
    None,
    mandationStatus,
    None
  )

  val customerDetailsOrganisation = CustomerDetails(
    None,
    None,
    Some(orgName),
    Some(tradingName),
    None,
    mandationStatus,
    None
  )

  val customerDetailsNonDigital = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    None,
    nonDigital,
    None
  )

  val customerDetailsOptedOut = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    None,
    nonMTDfB,
    Some(Deregistration(Some(LocalDate.parse("2019-01-01"))))
  )

  val customerDetailsPendingDeregestrationNoInfo = CustomerDetails(
    None,
    None,
    None,
    None,
    None,
    mandationStatus,
    None,
    Some(ChangeIndicators(true))
  )

  val customerDetailsFutureDeregisterOptedOut = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    None,
    nonMTDfB,
    Some(Deregistration(Some(LocalDate.parse("2020-01-01"))))
  )

  val customerDetailsFutureDeregisterOptedOutVatGroup = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName),
    Some(vatGroup),
    nonMTDfB,
    Some(Deregistration(Some(LocalDate.parse("2020-01-01"))))
  )

}
