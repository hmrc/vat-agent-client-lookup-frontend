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

import common.SessionKeys.viewedDDInterrupt
import config.{AppConfig, ErrorHandler}
import connectors.httpParsers.ResponseHttpParser.HttpResult
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentWithClient
import forms.DDInterruptForm

import javax.inject.{Inject, Singleton}
import models.DirectDebit
import models.errors.DirectDebitError
import play.api.Logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CustomerDetailsService, DateService, DirectDebitService}
import views.html.agent.{AgentHubView, DirectDebitInterruptView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentHubController @Inject()(val authenticate: AuthoriseAsAgentWithClient,
                                   val serviceErrorHandler: ErrorHandler,
                                   val customerDetailsService: CustomerDetailsService,
                                   val dateService: DateService,
                                   val directDebitService: DirectDebitService,
                                   mcc: MessagesControllerComponents,
                                   agentHubView: AgentHubView,
                                   ddInterruptView: DirectDebitInterruptView,
                                   implicit val appConfig: AppConfig,
                                   implicit val executionContext: ExecutionContext
                                  ) extends BaseController(mcc) {

  def show: Action[AnyContent] = authenticate.async { implicit user =>

    for {
      customerInfo <- customerDetailsService.getCustomerDetails(user.vrn)
      ddResult <-
        if(appConfig.features.directDebitInterruptFeature() && user.session.get(viewedDDInterrupt).isEmpty) {
          directDebitService.getDirectDebit(user.vrn)
        } else {
          Future.successful(Left(DirectDebitError))
        }
    } yield {

      val hasNotViewedDDInterrupt = user.session.get(viewedDDInterrupt).isEmpty

      customerInfo match {
        case Right(details) =>
          (details.missingTrader, details.hasPendingPPOB, ddInterrupt(ddResult)) match {
            case (_, _, true) if hasNotViewedDDInterrupt => Ok(ddInterruptView(DDInterruptForm.form))
            case (true, false, _) => Redirect(appConfig.manageVatMissingTraderUrl)
            case _ => Ok(agentHubView(details, user.vrn, dateService.now()))
          }
        case Left(error) =>
          Logger.warn(s"[AgentHubController][show] - received an error from CustomerDetailsService: $error")
          serviceErrorHandler.showInternalServerError
      }
    }
  }

  private[controllers] def ddInterrupt(directDebitInfo: HttpResult[DirectDebit]): Boolean = {

    val ddStatus: Option[Boolean] = directDebitInfo.fold(_ => None, dd => Some(dd.directDebitMandateFound))

    ddStatus.contains(false)

  }
}
