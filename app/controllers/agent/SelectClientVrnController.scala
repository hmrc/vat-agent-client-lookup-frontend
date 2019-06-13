/*
 * Copyright 2019 HM Revenue & Customs
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

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentOnly
import forms.ClientVrnForm
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc._

@Singleton
class SelectClientVrnController @Inject()(val messagesApi: MessagesApi,
                                          val authenticate: AuthoriseAsAgentOnly,
                                          val serviceErrorHandler: ErrorHandler,
                                          implicit val appConfig: AppConfig) extends BaseController {

  def show(redirectUrl: String): Action[AnyContent] = authenticate { implicit agent =>

      val prefYes: Boolean = agent.session.get(SessionKeys.preference).fold(false)(_ == "yes")
      val prefNo: Boolean = agent.session.get(SessionKeys.preference).fold(false)(_ == "no")
      val hasVerifiedEmail: Boolean = agent.session.get(SessionKeys.verifiedAgentEmail).fold(false)(_.nonEmpty)
      val preferenceLogic: Boolean = prefNo || (prefYes && hasVerifiedEmail)

      if(appConfig.features.whereToGoFeature()) {
        extractRedirectUrl(redirectUrl).fold(Ok(views.html.agent.selectClientVrn(ClientVrnForm.form))) {
          url =>
            Ok(views.html.agent.selectClientVrn(ClientVrnForm.form)).addingToSession(SessionKeys.redirectUrl -> url)
        }
      //TODO: all remaining logic will become redundant and should be removed when whereToGoFeature permanently on
      } else if (preferenceLogic || !appConfig.features.preferenceJourneyEnabled()) {
        agent.session.get(SessionKeys.redirectUrl) match {
          case Some(_) =>
            Ok(views.html.agent.selectClientVrn(ClientVrnForm.form))
          case None =>
            val url: String = extractRedirectUrl(redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl)
            Ok(views.html.agent.selectClientVrn(ClientVrnForm.form)).addingToSession(SessionKeys.redirectUrl -> url)
        }
      } else {
        agent.session.get(SessionKeys.redirectUrl) match {
          case Some(_) =>
            Redirect(controllers.agent.routes.CapturePreferenceController.show())
          case None =>
            val url: String = extractRedirectUrl(redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl)
            Redirect(controllers.agent.routes.CapturePreferenceController.show())
              .addingToSession(SessionKeys.redirectUrl -> url)
        }
      }
  }

  val submit: Action[AnyContent] = authenticate {
    implicit agent =>
      ClientVrnForm.form.bindFromRequest().fold(
        error => {
          Logger.debug("[SelectClientVrnController][submit] Error")
          BadRequest(views.html.agent.selectClientVrn(error))
        },
        data => {
          Logger.debug("[SelectClientVrnController][submit] Success")
          Redirect(controllers.agent.routes.ConfirmClientVrnController.show())
            .removingFromSession(SessionKeys.clientMandationStatus)
            .addingToSession(SessionKeys.clientVRN -> data.vrn)
        }
      )
  }
}
