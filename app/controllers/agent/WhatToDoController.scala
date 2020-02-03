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

package controllers.agent

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentWithClient
import forms.WhatToDoForm
import javax.inject.{Inject, Singleton}
import models.User
import models.agent._
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{CustomerDetailsService, DateService}
import views.html.agent.WhatToDoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatToDoController @Inject()(val authenticate: AuthoriseAsAgentWithClient,
                                   val serviceErrorHandler: ErrorHandler,
                                   val customerDetailsService: CustomerDetailsService,
                                   val dateService: DateService,
                                   mcc: MessagesControllerComponents,
                                   whatToDoView: WhatToDoView,
                                   implicit val appConfig: AppConfig,
                                   implicit val executionContext: ExecutionContext) extends BaseController(mcc) {

  def show: Action[AnyContent] = authenticate.async { implicit user =>
    if(appConfig.features.useAgentHubPageFeature()) {
      Future(Redirect(controllers.agent.routes.AgentHubController.show()))
    } else {
      customerDetailsService.getCustomerDetails(user.vrn).map {
        case Right(details) =>
          Ok(whatToDoView(WhatToDoForm.whatToDoForm, details.clientName, details.mandationStatus))
            .addingToSession(SessionKeys.mtdVatAgentClientName -> details.clientName, SessionKeys.mtdVatAgentMandationStatus -> details.mandationStatus)
        case Left(error) =>
          Logger.warn(s"[WhatToDoController][show] - received an error from CustomerDetailsService: $error")
          serviceErrorHandler.showInternalServerError
      }
    }
  }


  def submit: Action[AnyContent] = authenticate.async {
    implicit user =>

      WhatToDoForm.whatToDoForm.bindFromRequest().fold(
        error => badRequestResult(error),
        data => Future.successful(
          data.value match {
            case SubmitReturn.value => Redirect(appConfig.returnDeadlinesUrl)
            case ViewReturn.value => Redirect(appConfig.submittedReturnsUrl)
            case ChangeDetails.value => emailPrefCheck(user)
            case ViewCertificate.value => Redirect(appConfig.vatCertificateUrl)
          }
        ).map(redirect => redirect.removingFromSession(
          SessionKeys.mtdVatAgentClientName,
          SessionKeys.mtdVatAgentMandationStatus)
        )
      )
  }

  private def badRequestResult(error: Form[WhatToDoModel])(implicit user: User[_]): Future[Result] = {
    (user.session.get(SessionKeys.mtdVatAgentClientName), user.session.get(SessionKeys.mtdVatAgentMandationStatus)) match {
      case (Some(clientName), Some(mandationStatus)) =>
        Future.successful(BadRequest(whatToDoView(error, clientName, mandationStatus)))
      case _ =>
        customerDetailsService.getCustomerDetails(user.vrn).map {
          case Right(details) => BadRequest(whatToDoView(error, details.clientName, details.mandationStatus))
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
      case (true, _) | (_, Some("no")) => Redirect(appConfig.manageVatCustomerDetailsUrl)
      case _ => Redirect(routes.CapturePreferenceController.show())
    }
  }
}
