/*
 * Copyright 2018 HM Revenue & Customs
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

import models.CustomerDetails
import play.api.libs.json.{JsObject, Json}

object CustomerDetailsTestConstants {

  val orgName = "Ancient Antiques Ltd"
  val tradingName = "Dusty Relics"
  val firstName = "Fred"
  val lastName = "Flintstone"

  val noInfoJson: JsObject = Json.obj()

  val allInfoJson: JsObject = Json.obj(
    "firstName" -> firstName,
    "lastName" -> lastName,
    "organisationName" -> orgName,
    "tradingName" -> tradingName
  )

  val customerDetailsAllInfo = CustomerDetails(
    Some(firstName),
    Some(lastName),
    Some(orgName),
    Some(tradingName)
  )

  val customerDetailsNoInfo = CustomerDetails(
    None,
    None,
    None,
    None
  )

  val customerDetailsFnameOnly = CustomerDetails(
    Some(firstName),
    None,
    None,
    None
  )

  val customerDetailsLnameOnly = CustomerDetails(
    None,
    Some(lastName),
    None,
    None
  )

  val customerDetailsNoTradingName = CustomerDetails(
    None,
    None,
    Some(orgName),
    None
  )

  val customerDetailsIndividual = CustomerDetails(
    Some(firstName),
    Some(lastName),
    None,
    None
  )

  val customerDetailsOrganisation = CustomerDetails(
    None,
    None,
    Some(orgName),
    Some(tradingName)
  )
}
