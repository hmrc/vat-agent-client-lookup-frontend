/*
 * Copyright 2025 HM Revenue & Customs
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

import com.google.inject.Inject
import models.ChangedOnVatPeriod.RequestCategoryType4
import models.{StandingRequest, StandingRequestDetail}
import utils.LoggingUtil

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

class AnnualAccountingCheckService @Inject() () extends LoggingUtil {

  def changedOnDateWithinLast3Months(standingRequestScheduleOpt: Option[StandingRequest], today: LocalDate): Option[LocalDate] = {
    standingRequestScheduleOpt match {
      case Some(standingRequestSchedule) =>
        standingRequestSchedule.standingRequests
          .filter(_.requestCategory.equals(RequestCategoryType4))
          .flatMap(sr => doesSrChangedOnFallWithinLastNMonths(sr, today, 3))
          .sorted
          .reverse
          .headOption match {
          case Some(date) =>
            logger.debug(s"[changedOnDateWithinLast3Months]: condition passed for $standingRequestSchedule")
            Some(date)
          case None =>
            logger.debug(s"[changedOnDateWithinLast3Months]: condition failed for $standingRequestSchedule")
            None
        }
      case None =>
        logger.debug(s"[changedOnDateWithinLast3Months]: empty standing request schedule")
        None
    }
  }

  private def doesSrChangedOnFallWithinLastNMonths(standingRequest: StandingRequestDetail, today: LocalDate, months: Int): Option[LocalDate] = {
    val dateFormat: String = "yyyy-MM-dd"
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
    val changedOnOpt = standingRequest.changedOn.flatMap(d => Try(LocalDate.parse(d, formatter)).toOption)
    val thresholdDate = today.minusMonths(months.toLong)

    if (changedOnOpt.exists(_.isAfter(thresholdDate))) changedOnOpt else None
  }
}
