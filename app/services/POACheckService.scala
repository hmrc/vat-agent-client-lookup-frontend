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
import models.ChangedOnVatPeriod.RequestCategoryType3
import models.{ChangedOnVatPeriod, StandingRequest, StandingRequestDetail}
import utils.LoggingUtil

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

class POACheckService @Inject()() extends LoggingUtil {

  def changedOnDateWithInLatestVatPeriod(standingRequestScheduleOpt: Option[StandingRequest], today: LocalDate): Option[LocalDate] = {
    standingRequestScheduleOpt match {
      case Some(standingRequestSchedule) =>
        standingRequestSchedule.standingRequests.filter(_.requestCategory.equals(RequestCategoryType3)).flatMap(sr =>
          doesSrChangedOnFallsInLatestVatPeriod(sr, today)).sorted.reverse.headOption match {
          case Some(date) =>
            logger.debug(
              s"[changedOnDateWithInLatestVatPeriod]: Date condition for Changed On passed, with  standingRequestSchedule ($standingRequestSchedule)"
            )
            Some(date)
          case None =>
            logger.debug(
              s"[changedOnDateWithInLatestVatPeriod]: Date condition for Changed On failed, with  standingRequestSchedule ($standingRequestSchedule)"
            )
            None
        }
      case None =>
        logger.debug(
          s"[changedOnDateWithInLatestVatPeriod]: standingRequestSchedule for this customer is empty"
        )
        None
    }
  }

  private def doesSrChangedOnFallsInLatestVatPeriod(standingRequest: StandingRequestDetail, today: LocalDate): Option[LocalDate] = {
    val dateFormat: String = "yyyy-MM-dd"
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
    val periods = standingRequest.requestItems
      .groupBy(_.periodKey)
      .toSeq.sortBy(_._1)
      .map { case (periodKey, items) =>
        val startDate = items.head.startDate
        val endDate = items.last.endDate

        val startDateOpt = Try(LocalDate.parse(startDate, formatter)).toOption
        val endDateOpt = Try(LocalDate.parse(endDate, formatter)).toOption

        val isCurrent: Boolean = today.isEqual(startDateOpt.get) ||
          (today.isAfter(startDateOpt.get) && today.isBefore(endDateOpt.get)) || today.isEqual(endDateOpt.get)

        ChangedOnVatPeriod(
          startDate = startDateOpt,
          endDate = endDateOpt,
          isCurrent = isCurrent
        )
      }
    logger.debug(s"ak.....$standingRequest")
    logger.debug(s"ak.....$periods")
    val changedOnOpt = if (standingRequest.changedOn.isDefined) Try(LocalDate.parse(standingRequest.changedOn.get, formatter)).toOption else None

    if (changedOnOpt.isDefined && periods.exists(_.isCurrent) &&
      !changedOnOpt.get.isBefore(periods.filter(_.isCurrent).head.startDate.get) &&
      !changedOnOpt.get.isAfter(periods.filter(_.isCurrent).head.endDate.get)) {
      changedOnOpt
    } else {
      None
    }
  }
}
