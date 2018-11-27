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

import common.SessionKeys
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentOnly
import forms.PreferenceForm._
import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc._

import scala.concurrent.Future

@Singleton
class CapturePreferenceController @Inject()(val messagesApi: MessagesApi,
                                            val authenticate: AuthoriseAsAgentOnly,
                                            implicit val appConfig: AppConfig) extends BaseController {

  def show: Action[AnyContent] = authenticate.async { implicit user =>
    Future.successful(Ok(views.html.agent.capturePreference(preferenceForm)))
  }

  def submit: Action[AnyContent] = authenticate.async { implicit user =>
    preferenceForm.bindFromRequest().fold(
      error     => Future.successful(BadRequest(views.html.agent.capturePreference(error))),
      formData  => {
        if (formData.yesNo.value) {
          Future.successful(Redirect(controllers.agent.routes.SelectClientVrnController.show())
            .addingToSession(SessionKeys.preference -> yes)
            .addingToSession(SessionKeys.email      -> formData.email.getOrElse("")))
        } else {
          Future.successful(Redirect(controllers.agent.routes.SelectClientVrnController.show())
            .addingToSession(SessionKeys.preference -> no))
        }
      }
    )
  }
}
