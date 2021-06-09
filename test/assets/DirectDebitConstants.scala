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

import connectors.httpParsers.ResponseHttpParser.HttpResult
import models.DirectDebit

object DirectDebitConstants {

  val ddNoMandateFound: HttpResult[DirectDebit] = Right(DirectDebit(
    directDebitMandateFound = false
  ))

  val ddMandateFound: HttpResult[DirectDebit] = Right(DirectDebit(
    directDebitMandateFound = true
  ))

  val ddFailureResponse = Left(models.errors.UnexpectedError(500, "problems"))
}
