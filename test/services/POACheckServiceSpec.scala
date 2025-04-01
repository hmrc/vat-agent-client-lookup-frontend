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

package services

import models.{RequestItem, StandingRequest, StandingRequestDetail}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class POACheckServiceSpec extends AnyWordSpec with Matchers {

  val service = new POACheckService()

  val fixedDate: LocalDate = LocalDate.parse("2018-05-01")
  val todayFixedDate: LocalDate = LocalDate.parse("2025-02-28")


  val modelSrChangedOnTest1: StandingRequest = StandingRequest(
    "2025-03-15", List(
      StandingRequestDetail(
        requestNumber = "20000037272",
        requestCategory = "3",
        createdOn = "2025-03-15",
        changedOn = Some("2025-03-15"),
        requestItems = List(
          RequestItem(
            period = "1",
            periodKey = "25A1",
            startDate = "2025-01-01",
            endDate = "2025-02-02",
            dueDate = "2025-02-02",
            amount = 25000.50,
            chargeReference = Some("XD006411191344"),
            postingDueDate = None
          ),
          RequestItem(
            period = "2",
            periodKey = "25A1",
            startDate = "2025-02-01",
            endDate = "2025-03-31",
            dueDate = "2025-03-31",
            amount = 20000.75,
            chargeReference = Some("XD006411191345"),
            postingDueDate = Some("2024-04-30")
          )
        )
      ),
      StandingRequestDetail(
        requestNumber = "20000037273",
        requestCategory = "2",
        createdOn = "2023-11-30",
        changedOn = Some("2025-02-01"),
        requestItems = List(
          RequestItem(
            period = "1",
            periodKey = "25A1",
            startDate = "2025-04-01",
            endDate = "2025-06-30",
            dueDate = "2025-06-30",
            amount = 22945.23,
            chargeReference = Some("XD006411191344"),
            postingDueDate = Some("2025-06-30")
          )
        )
      )
    )
  )

  val modelSrChangedOnTest2: StandingRequest = StandingRequest(
    ("2025-03-15"), List(
      StandingRequestDetail(
        requestNumber = "20000037272",
        requestCategory = "3",
        createdOn = ("2025-03-15"),
        changedOn = Some("2025-03-15"),
        requestItems = List(
          RequestItem(
            period = "1",
            periodKey = "25A1",
            startDate = ("2025-01-01"),
            endDate = ("2025-02-02"),
            dueDate = ("2025-02-02"),
            amount = 25000.50,
            chargeReference = Some("XD006411191344"),
            postingDueDate = None
          )
        )
      ),
      StandingRequestDetail(
        requestNumber = "20000037273",
        requestCategory = "2",
        createdOn = ("2023-11-30"),
        changedOn = Some("2025-02-01"),
        requestItems = List(
          RequestItem(
            period = "1",
            periodKey = "25A1",
            startDate = ("2025-04-01"),
            endDate = ("2025-06-30"),
            dueDate = ("2025-06-30"),
            amount = 22945.23,
            chargeReference = Some("XD006411191344"),
            postingDueDate = Some("2025-06-30")
          )
        )
      )
    )
  )

  "changedOnDateWithInLatestVatPeriod" should {
    "should return the valid changed on date" in {
      val result = service.changedOnDateWithInLatestVatPeriod(
        Some(modelSrChangedOnTest1),
        todayFixedDate
      )
      result shouldBe Some(LocalDate.parse("2025-03-15"))
    }
    "should return none for the changed on date not falling in current valid vat period" in {
      val result = service.changedOnDateWithInLatestVatPeriod(
        Some(modelSrChangedOnTest2),
        todayFixedDate
      )
      result shouldBe None
    }
  }
}
