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

package controllers

import config.AppConfig
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._
import uk.gov.hmrc.play.bootstrap.binders.{AbsoluteWithHostnameFromAllowlist, OnlyRelative, RedirectUrl}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

abstract class BaseController(val mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def extractRedirectUrl(url: String)(implicit appConfig: AppConfig): Option[String] = {

    try {
      if (url.nonEmpty) {
        RedirectUrl(url).getEither(OnlyRelative | AbsoluteWithHostnameFromAllowlist(appConfig.environmentHost)) match {
          case Right(value) =>
            Some(value.toString())
          case Left(_) =>
            Logger.warn("[BaseController][extractRedirectUrl] redirectUrl was an invalid absolute url")
            None
        }
      } else {
        Logger.info("[BaseController][extractRedirectUrl] couldn't create ContinueUrl from empty string.")
        None
      }
    } catch {
      case e: Exception =>
        Logger.warn("[BaseController][extractRedirectUrl] couldn't create ContinueUrl from what was provided.", e)
        None
    }
  }
}
