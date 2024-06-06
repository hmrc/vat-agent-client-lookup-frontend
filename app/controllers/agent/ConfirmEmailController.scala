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

import javax.inject.{Inject, Singleton}
import audit.AuditService
import audit.models.YesPreferenceVerifiedAuditModel
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.{AuthoriseAsAgentOnly, PreferencePredicate}
import models.Agent
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, RequestHeader, Result}
import services.EmailVerificationService
import utils.LoggingUtil
import views.html.agent.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmEmailController @Inject()(authenticate: AuthoriseAsAgentOnly,
                                       preferenceCheck: PreferencePredicate,
                                       emailVerificationService: EmailVerificationService,
                                       errorHandler: ErrorHandler,
                                       auditService: AuditService,
                                       mcc: MessagesControllerComponents,
                                       confirmEmailView: CheckYourAnswersView)
                                      (implicit executionContext: ExecutionContext,
                                       appConfig: AppConfig) extends BaseController(mcc) with LoggingUtil {

  def show: Action[AnyContent] = (authenticate andThen preferenceCheck) { implicit agent =>
    agent.session.get(SessionKeys.notificationsEmail) match {
      case Some(email) =>
        Ok(confirmEmailView(email))
      case _ =>
        Redirect(routes.CapturePreferenceController.show())
    }
  }

  def isEmailVerified: Action[AnyContent] = (authenticate andThen preferenceCheck).async { implicit agent =>
    agent.session.get(SessionKeys.notificationsEmail) match {
      case Some(email) =>
        emailVerificationService.isEmailVerified(email) map {
          case Some(true) =>
            handleRedirect(agent, email)
          case Some(false) =>
            warnLog("[ConfirmEmailController][isEmailVerified] - email not verified")
            Redirect(routes.VerifyEmailPinController.requestPasscode)
          case _ =>
            errorLog("[ConfirmEmailController][isEmailVerified] - could not get the email verification state")
            errorHandler.showInternalServerError
        }
      case _ =>
        errorLog("[ConfirmEmailController][updateNotificationPreference] no email address found in session")
        Future.successful(Redirect(routes.CapturePreferenceController.show()))
    }
  }

  private def handleRedirect(agent: Agent[AnyContent], email: String)(implicit rh: RequestHeader, request: Request[_]): Result = {
    val redirectUrl = agent.session.get(SessionKeys.redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl)

    auditService.extendedAudit(
      YesPreferenceVerifiedAuditModel(agent.arn, email),
      Some(controllers.agent.routes.ConfirmEmailController.isEmailVerified.url)
    )
    Redirect(redirectUrl).addingToSession(SessionKeys.verifiedEmail -> email)
  }
}
