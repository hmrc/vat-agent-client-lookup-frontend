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

import audit.AuditService
import audit.models.YesPreferenceVerifiedAuditModel
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthoriseAsAgentOnly, PreferencePredicate}
import javax.inject.{Inject, Singleton}
import models.Agent
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.EmailVerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.agent.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmEmailController @Inject()(val authenticate: AuthoriseAsAgentOnly,
                                       val preferenceCheck: PreferencePredicate,
                                       val emailVerificationService: EmailVerificationService,
                                       val errorHandler: ErrorHandler,
                                       val auditService: AuditService,
                                       mcc: MessagesControllerComponents,
                                       confirmEmailView: CheckYourAnswersView,
                                       implicit val executionContext: ExecutionContext,
                                       implicit val appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

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
          case Some(false) => {
              Redirect(routes.VerifyEmailPinController.requestPasscode())
            }

          case _ =>
            errorHandler.showInternalServerError
        }
      case _ =>
        Logger.info("[ConfirmEmailController][updateNotificationPreference] no email address found in session")
        Future.successful(Redirect(routes.CapturePreferenceController.show()))
    }
  }

  private def handleRedirect(agent: Agent[AnyContent], email: String)(implicit rh: RequestHeader): Result = {
    val redirectUrl = agent.session.get(SessionKeys.redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl)

    auditService.extendedAudit(
      YesPreferenceVerifiedAuditModel(agent.arn, email),
      Some(controllers.agent.routes.ConfirmEmailController.isEmailVerified().url)
    )
    Redirect(redirectUrl).addingToSession(SessionKeys.verifiedAgentEmail -> email)
  }
}
