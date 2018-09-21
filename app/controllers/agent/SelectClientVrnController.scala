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

package controllers.agent

import javax.inject.{Inject, Singleton}

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthoriseAsAgentOnly
import forms.ClientVrnForm
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.binders.ContinueUrl
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class SelectClientVrnController @Inject()(val messagesApi: MessagesApi,
                                          val authenticate: AuthoriseAsAgentOnly,
                                          val serviceErrorHandler: ErrorHandler,
                                          implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def show(redirectUrl: String): Action[AnyContent] = authenticate.async {
    implicit agent =>
      agent.session.get(SessionKeys.redirectUrl) match {
        case Some(_) =>
          Future.successful(Ok(views.html.agent.selectClientVrn(ClientVrnForm.form)))
        case None =>
          extractRedirectUrl(redirectUrl) match {
            case Some(url) =>
              Future.successful(Ok(views.html.agent.selectClientVrn(ClientVrnForm.form))
                .addingToSession(SessionKeys.redirectUrl -> url))
            case None =>
              Future.successful(serviceErrorHandler.showInternalServerError)
          }
      }
  }

  val submit: Action[AnyContent] = authenticate.async {
    implicit agent =>
      ClientVrnForm.form.bindFromRequest().fold(
        error => {
          Logger.debug(s"[SelectClientVrnController][submit] Error")
          Future.successful(BadRequest(views.html.agent.selectClientVrn(error)))
        },
        data => {
          Logger.debug(s"[SelectClientVrnController][submit] Success")
          Future.successful(Redirect(controllers.agent.routes.ConfirmClientVrnController.show())
            .addingToSession(SessionKeys.clientVRN -> data.vrn))
        }
      )
  }

  private[controllers] def extractRedirectUrl(url: String): Option[String] = {
    try {
      val continueUrl = ContinueUrl(url)
      if (continueUrl.isRelativeUrl || url.startsWith(appConfig.environmentBase)) {
        Some(url)
      } else {
        Logger.warn("[JourneySetupController][journeySetup] redirectUrl was empty or an invalid absolute url")
        None
      }
    } catch {
      case e: Exception =>
        Logger.warn("[JourneySetupController][journeySetup] couldn't create ContinueUrl from what was provided.", e)
        None
    }
  }
}
