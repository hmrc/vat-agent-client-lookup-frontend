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
import controllers.BaseController
import controllers.predicates.{AuthoriseAsAgentWithClient, DDInterruptPredicate}
import javax.inject.{Inject, Singleton}
import models.errors._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomerDetailsService
import views.html.agent.ConfirmClientVrnView
import views.html.errors.{AccountMigrationView, NotSignedUpView}

import scala.concurrent.ExecutionContext

@Singleton
class ConfirmClientVrnController @Inject()(authenticate: AuthoriseAsAgentWithClient,
                                           ddInterrupt: DDInterruptPredicate,
                                           customerDetailsService: CustomerDetailsService,
                                           errorHandler: ErrorHandler,
                                           auditService: AuditService,
                                           mcc: MessagesControllerComponents,
                                           confirmClientVrnView: ConfirmClientVrnView,
                                           accountMigrationView: AccountMigrationView,
                                           notSignedUpView: NotSignedUpView)
                                          (implicit appConfig: AppConfig,
                                           executionContext: ExecutionContext) extends BaseController(mcc) {

  def show: Action[AnyContent] = authenticate.async {
    implicit user =>
      customerDetailsService.getCustomerDetails(user.vrn) map {
        case Right(customerDetails) if customerDetails.isInsolvent =>
          logger.debug("[ConfirmClientVrnController][show] - Client is insolvent, rendering UnauthorisedForClient page")
          Redirect(controllers.agent.routes.AgentUnauthorisedForClientController.show())
        case Right(customerDetails) =>
          auditService.extendedAudit(
            AuthenticateAgentAuditModel(user.arn.get, user.vrn, isAuthorisedForClient = true),
            Some(controllers.agent.routes.ConfirmClientVrnController.show.url)
          )
          auditService.extendedAudit(
            GetClientBusinessNameAuditModel(user.arn.get, user.vrn, customerDetails.clientName),
            Some(controllers.agent.routes.ConfirmClientVrnController.show.url)
          )

          Ok(confirmClientVrnView(user.vrn, customerDetails))
            .addingToSession(SessionKeys.clientName -> customerDetails.clientName)

        case Left(Migration) => PreconditionFailed(accountMigrationView())
        case Left(NotSignedUp) => NotFound(notSignedUpView())
        case _ =>
          logger.warn("[ConfirmClientVrnController][show] Error returned from GetCustomerDetails")
          errorHandler.showInternalServerError
      }
  }

  def changeClient: Action[AnyContent] = authenticate {
    implicit user =>
      val redirectUrl = user.session.get(SessionKeys.redirectUrl).getOrElse("")

      Redirect(controllers.agent.routes.SelectClientVrnController.show(redirectUrl))
        .removingFromSession(SessionKeys.clientVRN, SessionKeys.viewedDDInterrupt, SessionKeys.clientName)
  }

  def redirect: Action[AnyContent] = (authenticate andThen ddInterrupt) {

    implicit user =>

      val redirectUrl = user.session.get(SessionKeys.redirectUrl)
      val hasVerifiedAgentEmail: Boolean = user.session.get(SessionKeys.verifiedEmail).isDefined
      val manageVatUrl = appConfig.manageVatCustomerDetailsUrl

      redirectUrl match {

        case Some(changeUrl) if changeUrl.contains("/vat-through-software/account")  =>
          user.session.get(SessionKeys.preference) match {
            case Some("yes") if !hasVerifiedAgentEmail =>
              Redirect(controllers.agent.routes.ConfirmEmailController.isEmailVerified)
            case Some(_) => Redirect(manageVatUrl).removingFromSession(SessionKeys.redirectUrl)
            case None => Redirect(controllers.agent.routes.CapturePreferenceController.show())
          }

        case Some("") | None =>
          logger.debug("[ConfirmClientVrnController][redirect] Redirect url not provided. " +
            "Redirecting to 'Agent Hub' page.")
          Redirect(controllers.agent.routes.AgentHubController.show)

        case Some(nonChangeUrl) =>
          Redirect(nonChangeUrl).removingFromSession(SessionKeys.redirectUrl)
      }
  }
}
