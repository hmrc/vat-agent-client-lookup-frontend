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

package controllers.agent

import common.SessionKeys
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentWithClient
import forms.DDInterruptForm
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.agent.DirectDebitInterruptView

import javax.inject.{Inject, Singleton}

@Singleton
class DDInterruptController @Inject()(mcc: MessagesControllerComponents,
                                      authenticate: AuthoriseAsAgentWithClient,
                                      ddInterruptView: DirectDebitInterruptView)
                                     (implicit val appConfig: AppConfig) extends BaseController(mcc) {

  val submit: Action[AnyContent] = authenticate { implicit agent =>
    (appConfig.features.directDebitInterruptFeature(), agent.session.get(SessionKeys.viewedDDInterrupt).isDefined) match {
      case (true, true) | (false, _) =>
        Redirect(controllers.agent.routes.AgentHubController.show())
      case (true, _) =>
        DDInterruptForm.form.bindFromRequest().fold(
          error => BadRequest(ddInterruptView(error)),
          _ => Redirect(controllers.agent.routes.AgentHubController.show())
                .addingToSession(SessionKeys.viewedDDInterrupt -> "true")
        )
    }
  }
}
