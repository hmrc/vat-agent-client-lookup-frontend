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

package controllers

import config.{AppConfig, ErrorHandler}
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.libs.json.{JsResultException, JsValue}
import common.SessionKeys
import controllers.predicates.AuthoriseAsAgentOnly
import play.api.Logger
import uk.gov.hmrc.play.binders.ContinueUrl
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class JourneySetupController @Inject()(val messagesApi: MessagesApi,
                                       val authenticate: AuthoriseAsAgentOnly,
                                       val serviceErrorHandler: ErrorHandler,
                                       implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def journeySetup():Action[AnyContent] = authenticate.async {
    implicit request =>
    request.body.asJson match {

      case Some(json) =>
        val url = extractRedirectUrl(json).fold("")(_.self)
        try {
          val cUrl = ContinueUrl(url)
          if (cUrl.isRelativeUrl || url.startsWith(appConfig.environmentBase)) {
            Future.successful(Redirect(controllers.agent.routes.SelectClientVrnController.show())
              .addingToSession(SessionKeys.redirectUrl -> url))
          } else {
            Logger.warn("[JourneySetupController][journeySetup] redirectUrl was empty or an invalid absolute url")
            Future.successful(BadRequest)
          }
        } catch {
          case e: Exception =>
            Logger.warn("[JourneySetupController][journeySetup] couldn't create ContinueUrl from what was provided.", e)
            Future.successful(BadRequest)
        }

      case None =>
        Logger.warn("[JourneySetupController][extractRedirectUrl] couldn't create JsValue from request body")
        Future.successful(BadRequest)
    }
  }

  private[controllers] def extractRedirectUrl(json: JsValue): Option[String] = {
    try {
      Some((json \ "redirectUrl").as[String])
    } catch {
      case jsEx:JsResultException =>
        Logger.warn("[JourneySetupController][extractRedirectUrl] Couldn't find redirectUrl key in json provided", jsEx)
        None

      case e: Exception =>
        Logger.warn("[JourneySetupController][extractRedirectUrl] Encountered an unknown error", e)
        None
    }

  }
}
