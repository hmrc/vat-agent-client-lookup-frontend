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

import assets.CustomerDetailsTestConstants._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerDetailsSpec extends AnyWordSpecLike with Matchers {

  "CustomerDetailsModel" when {

    "calling .username" when {

      "FirstName and Lastname are present" should {

        "return 'Firstname Lastname'" in {
          customerDetailsIndividual.userName shouldBe Some(s"$firstName $lastName")
        }
      }

      "FirstName is present" should {

        "return 'Firstname'" in {
          customerDetailsFnameOnly.userName shouldBe Some(s"$firstName")
        }
      }

      "LastName is present" should {

        "return 'Lastname'" in {
          customerDetailsLnameOnly.userName shouldBe Some(s"$lastName")
        }
      }

      "No names are present" should {

        "return None" in {
          customerDetailsNoInfo.userName shouldBe None
        }
      }
    }

    "calling .clientName" when {

      "Trading name is present" should {

        "return Trading Name" in {
          customerDetailsAllInfo.clientName shouldBe tradingName
        }
      }

      "Trading name is not present" should {

        "return the organisation name" in {
          customerDetailsNoTradingName.clientName shouldBe orgName
        }
      }

      "Trading name and organisationName are not present" should {

        "return the first and last name" in {
          customerDetailsIndividual.clientName shouldBe s"$firstName $lastName"
        }
      }

      "no names are present" should {

        "throw an exception" in {
          intercept[Exception](customerDetailsNoInfo.clientName)
        }
      }
    }

    "Deserialize from JSON" when {

      "all optional fields are populated" in {
        allInfoJson.as[CustomerDetails] shouldBe customerDetailsAllInfo
      }

      "no optional fields are returned" in {
        noOptionalInfoJson.as[CustomerDetails] shouldBe customerDetailsNoInfo
      }

      "there are pending fields" in {
        pendingInfoJson.as[CustomerDetails] shouldBe customerDetailsAllInfo
      }
    }
  }

  "Calling .isInsolventWithoutAccess" when {

    "the user is insolvent and not continuing to trade" should {

      val model = customerDetailsNoInfo.copy(isInsolvent = true, continueToTrade = Some(false))

      "return true if there is no insolvency type" in {
        model.isInsolventWithoutAccess shouldBe true
      }

      "return false if the insolvency type is exempt" in {
        model.exemptInsolvencyTypes.foreach { iType =>
          model.copy(insolvencyType = Some(iType)).isInsolventWithoutAccess shouldBe false
        }
      }

      "return true if the insolvency type is blocked" in {
        model.blockedInsolvencyTypes.foreach { iType =>
          model.copy(insolvencyType = Some(iType)).isInsolventWithoutAccess shouldBe true
        }
      }
    }

    "the user is insolvent and is continuing to trade" should {

      val model = customerDetailsNoInfo.copy(isInsolvent = true, continueToTrade = Some(true))

      "return false if there is no insolvency type" in {
        model.isInsolventWithoutAccess shouldBe false
      }

      "return false if the insolvency type is exempt" in {
        model.exemptInsolvencyTypes.foreach { iType =>
          model.copy(insolvencyType = Some(iType)).isInsolventWithoutAccess shouldBe false
        }
      }

      "return true if the insolvency type is blocked" in {
        model.blockedInsolvencyTypes.foreach { iType =>
          model.copy(insolvencyType = Some(iType)).isInsolventWithoutAccess shouldBe true
        }
      }
    }

    "the user is not insolvent" should {

      "return false, regardless of the continueToTrade flag or insolvency type" in {
        customerDetailsNoInfo.isInsolventWithoutAccess shouldBe false
        customerDetailsNoInfo.copy(continueToTrade = Some(false)).isInsolventWithoutAccess shouldBe false
        customerDetailsNoInfo.copy(continueToTrade = Some(true)).isInsolventWithoutAccess shouldBe false
        customerDetailsNoInfo.copy(insolvencyType = Some("08")).isInsolventWithoutAccess shouldBe false
      }
    }
  }
}
