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

package controllers.agent

import common.SessionKeys
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentOnly
import forms.ClientVrnForm

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import utils.LoggingUtil
import views.html.agent.SelectClientVrnView

@Singleton
class SelectClientVrnController @Inject()(authenticate: AuthoriseAsAgentOnly,
                                          mcc: MessagesControllerComponents,
                                          selectClientVrnView: SelectClientVrnView)
                                         (implicit appConfig: AppConfig) extends BaseController(mcc) with LoggingUtil {

  def show(redirectUrl: String): Action[AnyContent] = authenticate { implicit agent =>

    extractRedirectUrl(redirectUrl).fold(Ok(selectClientVrnView(ClientVrnForm.form))) {
      url =>
        Ok(selectClientVrnView(ClientVrnForm.form)).addingToSession(SessionKeys.redirectUrl -> url)
    }
  }

  val submit: Action[AnyContent] = authenticate {
    implicit agent =>
      ClientVrnForm.form.bindFromRequest().fold(
        error => {
          debug("[SelectClientVrnController][submit] Error")
          BadRequest(selectClientVrnView(error))
        },
        data => {
          debug("[SelectClientVrnController][submit] Success")
          Redirect(controllers.agent.routes.ConfirmClientVrnController.show)
            .removingFromSession(SessionKeys.clientMandationStatus)
            .addingToSession(SessionKeys.clientVRN -> data.vrn)
        }
      )
  }
}
