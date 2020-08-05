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

import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentWithClient
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CustomerDetailsService, DateService}
import views.html.agent.AgentHubView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentHubController @Inject()(val authenticate: AuthoriseAsAgentWithClient,
                                   val serviceErrorHandler: ErrorHandler,
                                   val customerDetailsService: CustomerDetailsService,
                                   val dateService: DateService,
                                   mcc: MessagesControllerComponents,
                                   agentHubView: AgentHubView,
                                   implicit val appConfig: AppConfig,
                                   implicit val executionContext: ExecutionContext) extends BaseController(mcc) {

  def show: Action[AnyContent] = authenticate.async { implicit user =>
    if(appConfig.features.useAgentHubPageFeature()){
      customerDetailsService.getCustomerDetails(user.vrn).map {
        case Right(details) =>
          if (details.missingTrader && appConfig.features.missingTraderAddressIntercept() && !details.hasPendingPPOB) {
            Redirect(appConfig.manageVatMissingTraderUrl)
          } else {
            Ok(agentHubView(details, user.vrn, dateService.now(), dateService.isPostCovidDeadline))
          }
        case Left(error) =>
          Logger.warn(s"[AgentHubController][show] - received an error from CustomerDetailsService: $error")
          serviceErrorHandler.showInternalServerError
      }
    } else {
      Future.successful(Redirect(routes.WhatToDoController.show()))
    }
  }
}
