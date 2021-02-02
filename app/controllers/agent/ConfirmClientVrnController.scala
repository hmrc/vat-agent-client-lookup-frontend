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
import audit.models.{AuthenticateAgentAuditModel, GetClientBusinessNameAuditModel}
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthoriseAsAgentWithClient
import javax.inject.{Inject, Singleton}
import models.User
import models.errors._
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.CustomerDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.agent.ConfirmClientVrnView
import views.html.errors.{AccountMigrationView, NotSignedUpView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmClientVrnController @Inject()(val authenticate: AuthoriseAsAgentWithClient,
                                           val customerDetailsService: CustomerDetailsService,
                                           val errorHandler: ErrorHandler,
                                           val auditService: AuditService,
                                           mcc: MessagesControllerComponents,
                                           confirmClientVrnView: ConfirmClientVrnView,
                                           accountMigrationView: AccountMigrationView,
                                           notSignedUpView: NotSignedUpView,
                                           implicit val appConfig: AppConfig,
                                           implicit val executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = authenticate.async {
    implicit user =>
      customerDetailsService.getCustomerDetails(user.vrn) map {
        case Right(customerDetails) if customerDetails.isInsolventWithoutAccess =>
          Logger.debug("[ConfirmClientVrnController][show] - Client is insolventWithoutAccess, rendering UnauthorisedForClient page")
          Redirect(controllers.agent.routes.AgentUnauthorisedForClientController.show())
        case Right(customerDetails) =>
          auditService.extendedAudit(
            AuthenticateAgentAuditModel(user.arn.get, user.vrn, isAuthorisedForClient = true),
            Some(controllers.agent.routes.ConfirmClientVrnController.show().url)
          )
          auditService.extendedAudit(
            GetClientBusinessNameAuditModel(user.arn.get, user.vrn, customerDetails.clientName),
            Some(controllers.agent.routes.ConfirmClientVrnController.show().url)
          )

          Ok(confirmClientVrnView(user.vrn, customerDetails))
        case Left(Migration) => PreconditionFailed(accountMigrationView())
        case Left(NotSignedUp) => NotFound(notSignedUpView())
        case _ =>
          Logger.warn("[ConfirmClientVrnController][show] Error returned from GetCustomerDetails")
          errorHandler.showInternalServerError
      }
  }

  def changeClient: Action[AnyContent] = authenticate {
    implicit user =>
      val redirectUrl = user.session.get(SessionKeys.redirectUrl).getOrElse("")

      Redirect(controllers.agent.routes.SelectClientVrnController.show(redirectUrl))
        .removingFromSession(SessionKeys.clientVRN)
  }

  def redirect: Action[AnyContent] = authenticate {

    implicit user =>

      val redirectUrl = user.session.get(SessionKeys.redirectUrl)
      val hasVerifiedAgentEmail: Boolean = user.session.get(SessionKeys.verifiedAgentEmail).isDefined
      val manageVatUrl = appConfig.manageVatCustomerDetailsUrl

      redirectUrl match {

        case Some(changeUrl) if changeUrl.contains("/vat-through-software/account")  =>
          user.session.get(SessionKeys.preference) match {
            case Some("yes") if !hasVerifiedAgentEmail =>
              Redirect(controllers.agent.routes.ConfirmEmailController.isEmailVerified())
            case Some(_) => Redirect(manageVatUrl).removingFromSession(SessionKeys.redirectUrl)
            case None => Redirect(controllers.agent.routes.CapturePreferenceController.show())
          }

        case Some("") | None =>
          Logger.debug("[ConfirmClientVrnController][redirect] Redirect url not provided. " +
            "Redirecting to 'Agent Hub' page.")
          Redirect(controllers.agent.routes.AgentHubController.show())

        case Some(nonChangeUrl) =>
          Redirect(nonChangeUrl).removingFromSession(SessionKeys.redirectUrl)
      }
  }
}
