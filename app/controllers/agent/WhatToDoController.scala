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

import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentOnly
import forms.WhatToDoForm
import javax.inject.{Inject, Singleton}
import models.Agent
import models.agent._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

@Singleton
class WhatToDoController @Inject()(val messagesApi: MessagesApi,
                                   val authenticate: AuthoriseAsAgentOnly,
                                   val serviceErrorHandler: ErrorHandler,
                                   implicit val appConfig: AppConfig) extends BaseController {

  private def renderView(data: Form[WhatToDoModel] = WhatToDoForm.whatToDoForm)(implicit agent: Agent[_]) =
    views.html.agent.whatToDo(data)

  def show: Action[AnyContent] = authenticate { implicit agent =>
    if(appConfig.features.whereToGoFeature()){
      Ok(renderView())
    } else {
      Ok(views.html.errors.standardError(appConfig, "", "", "not found-arino"))
    }
  }


  def submit: Action[AnyContent] = authenticate {
    implicit agent =>
      WhatToDoForm.whatToDoForm.bindFromRequest().fold(
        error => BadRequest(renderView(error)),
        data => data.value match {
          case SubmitReturn.value => Ok("1")
          case ViewReturn.value => Ok("2")
          case ChangeDetails.value => Ok("3")
          case ViewCertificate.value => Ok("4")
        }
      )
  }
}
