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
import controllers.predicates.AuthoriseAsAgentWithClient
import forms.WhatToDoForm
import javax.inject.{Inject, Singleton}
import common.MandationStatus.nonMTDfB
import common.SessionKeys
import models.agent._
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomerDetailsService

import scala.concurrent.Future

@Singleton
class WhatToDoController @Inject()(val messagesApi: MessagesApi,
                                   val authenticate: AuthoriseAsAgentWithClient,
                                   val serviceErrorHandler: ErrorHandler,
                                   val customerDetailsService: CustomerDetailsService,
                                   implicit val appConfig: AppConfig) extends BaseController {

  def show: Action[AnyContent] = authenticate.async { implicit user =>
    if(appConfig.features.whereToGoFeature()){
      customerDetailsService.getCustomerDetails(user.vrn).map {
        case Right(details) =>
          Ok(views.html.agent.whatToDo(WhatToDoForm.whatToDoForm, details.clientName, details.mandationStatus == nonMTDfB))
            .addingToSession(SessionKeys.mtdVatAgentClientName -> details.clientName, SessionKeys.mtdVatAgentMandationStatus -> details.mandationStatus)
        case Left(error) =>
          Logger.warn(s"[WhatToDoController][show] - received an error from CustomerDetailsService: $error")
          serviceErrorHandler.showInternalServerError
      }
    } else {
      Future.successful(serviceErrorHandler.showInternalServerError)
    }
  }


  def submit: Action[AnyContent] = authenticate {
    implicit user =>
      val clientName: String = user.session.get(SessionKeys.mtdVatAgentClientName).fold("no name")(a => a)
      val isNonMTfB: Boolean = user.session.get(SessionKeys.mtdVatAgentMandationStatus).fold(false)(_ == nonMTDfB)
      if (appConfig.features.whereToGoFeature()) {
        WhatToDoForm.whatToDoForm.bindFromRequest().fold(
          error => BadRequest(views.html.agent.whatToDo(error, clientName, isNonMTfB)),
          data => {
            data.value match {
              case SubmitReturn.value => Redirect(appConfig.returnDeadlinesUrl)
              case ViewReturn.value => Redirect(appConfig.submittedReturnsUrl(DateTime.now(DateTimeZone.UTC).year().get()))
              case ChangeDetails.value => Redirect(appConfig.manageVatCustomerDetailsUrl)
              case ViewCertificate.value => Redirect(appConfig.vatCertificateUrl)
            }
          }
        ).removingFromSession(SessionKeys.mtdVatAgentClientName, SessionKeys.mtdVatAgentMandationStatus)
      } else {
        serviceErrorHandler.showInternalServerError
      }
  }
}
