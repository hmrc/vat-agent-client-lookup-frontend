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

package services

import java.time.LocalDate

import javax.inject.{Inject, Singleton}

@Singleton
class DateService @Inject()(appConfig: config.AppConfig) {

  def now(): LocalDate = {

    val staticDateEnabled: Boolean = appConfig.features.useStaticDateFeature()

    staticDateEnabled match {
      case true => LocalDate.parse(appConfig.staticDateValue)
      case false => LocalDate.now()
    }
  }

  def isPostCovidDeadline: Boolean = now().isAfter(LocalDate.of(2020,6,30))

}
