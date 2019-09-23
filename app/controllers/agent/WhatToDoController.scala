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
import models.User
import models.agent._
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.CustomerDetailsService

import scala.concurrent.Future

@Singleton
class WhatToDoController @Inject()(val messagesApi: MessagesApi,
                                   val authenticate: AuthoriseAsAgentWithClient,
                                   val serviceErrorHandler: ErrorHandler,
                                   val customerDetailsService: CustomerDetailsService,
                                   implicit val appConfig: AppConfig) extends BaseController {

  def show: Action[AnyContent] = authenticate.async { implicit user =>
    customerDetailsService.getCustomerDetails(user.vrn).map {
      case Right(details) =>
        Ok(views.html.agent.whatToDo(WhatToDoForm.whatToDoForm, details.clientName, details.mandationStatus == nonMTDfB))
          .addingToSession(SessionKeys.mtdVatAgentClientName -> details.clientName, SessionKeys.mtdVatAgentMandationStatus -> details.mandationStatus)
      case Left(error) =>
        Logger.warn(s"[WhatToDoController][show] - received an error from CustomerDetailsService: $error")
        serviceErrorHandler.showInternalServerError
    }
  }


  def submit: Action[AnyContent] = authenticate.async {
    implicit user =>

      WhatToDoForm.whatToDoForm.bindFromRequest().fold(
        error => badRequestResult(error),
        data => Future.successful(
          data.value match {
            case SubmitReturn.value => Redirect(appConfig.returnDeadlinesUrl)
            case ViewReturn.value => Redirect(appConfig.submittedReturnsUrl(DateTime.now(DateTimeZone.UTC).year().get()))
            case ChangeDetails.value => emailPrefCheck(user)
            case ViewCertificate.value => Redirect(appConfig.vatCertificateUrl)
          }
        ).removeSessionKey(SessionKeys.mtdVatAgentClientName)
          .removeSessionKey(SessionKeys.mtdVatAgentMandationStatus)
      )
  }

  private def badRequestResult(error: Form[WhatToDoModel])(implicit user: User[_]): Future[Result] = {
    (user.session.get(SessionKeys.mtdVatAgentClientName), user.session.get(SessionKeys.mtdVatAgentMandationStatus)) match {
      case (Some(clientName), Some(mandationStatus)) =>
        Future.successful(BadRequest(views.html.agent.whatToDo(error, clientName, mandationStatus == nonMTDfB)))
      case _ =>
        customerDetailsService.getCustomerDetails(user.vrn).map {
          case Right(details) => BadRequest(views.html.agent.whatToDo(error, details.clientName, details.mandationStatus == nonMTDfB))
          case Left(cdsError) =>
            Logger.warn(s"[WhatToDoController][submit] - received an error from CustomerDetailsService: $cdsError")
            serviceErrorHandler.showInternalServerError
        }
    }
  }

  private def emailPrefCheck: User[AnyContent] => Result = { implicit user: User[AnyContent] =>
    val hasVerifiedEmail = user.session.get(SessionKeys.verifiedAgentEmail).isDefined
    val emailPref = user.session.get(SessionKeys.preference)

    (hasVerifiedEmail, emailPref) match {
      case (true, _) => Redirect(appConfig.manageVatCustomerDetailsUrl)
      case (_, Some("no")) => Redirect(appConfig.manageVatCustomerDetailsUrl)
      case _ => Redirect(routes.CapturePreferenceController.show())
    }
  }
}
