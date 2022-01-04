/*
 * Copyright 2022 HM Revenue & Customs
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
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentWithClient
import forms.DDInterruptForm
import models.CustomerDetails
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CustomerDetailsService, DateService, FinancialDataService}
import views.html.agent.DirectDebitInterruptView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DDInterruptController @Inject()(mcc: MessagesControllerComponents,
                                      authenticate: AuthoriseAsAgentWithClient,
                                      ddInterruptView: DirectDebitInterruptView,
                                      dateService: DateService,
                                      customerDetailsService: CustomerDetailsService,
                                      directDebitService: FinancialDataService)
                                     (implicit appConfig: AppConfig,
                                      ec: ExecutionContext) extends BaseController(mcc) {

  def show: Action[AnyContent] = authenticate.async { implicit agent =>

    directDebitService.getDirectDebit(agent.vrn).flatMap { ddResult =>
      val hasDDSetup: String = ddResult.fold(_ => "", result => result.directDebitMandateFound.toString)
      if (appConfig.features.directDebitInterruptFeature()) {
        customerDetailsService.getCustomerDetails(agent.vrn).map {
          case Right(details) if migratedWithin4M(details) && hasDDSetup.contains("false") =>
            Ok(ddInterruptView(DDInterruptForm.form)).addingToSession(SessionKeys.mtdVatAgentDDMandateFound -> "false")
          case Right(details) if migratedWithin4M(details) && hasDDSetup.contains("true") =>
            Redirect(routes.ConfirmClientVrnController.redirect.url)
              .addingToSession(SessionKeys.viewedDDInterrupt -> "true", SessionKeys.mtdVatAgentDDMandateFound -> "true")
          case _ => Redirect(routes.ConfirmClientVrnController.redirect.url)
            .addingToSession(SessionKeys.viewedDDInterrupt -> "true", SessionKeys.mtdVatAgentDDMandateFound -> hasDDSetup)
        }
      } else {
        Future.successful(Redirect(routes.ConfirmClientVrnController.redirect.url)
          .addingToSession(SessionKeys.viewedDDInterrupt -> "true", SessionKeys.mtdVatAgentDDMandateFound -> hasDDSetup))
      }
    }
  }

  def submit: Action[AnyContent] = authenticate { implicit agent =>
    (appConfig.features.directDebitInterruptFeature(), agent.session.get(SessionKeys.viewedDDInterrupt).isDefined) match {
      case (true, true) | (false, _) =>
        Redirect(controllers.agent.routes.AgentHubController.show)
      case (true, _) =>
        DDInterruptForm.form.bindFromRequest().fold(
          error => BadRequest(ddInterruptView(error)),
          _ => Redirect(controllers.agent.routes.AgentHubController.show)
                .addingToSession(SessionKeys.viewedDDInterrupt -> "blueBox")
        )
    }
  }

  private[controllers] def migratedWithin4M(customerInfo: CustomerDetails): Boolean = {
    val monthLimit: Int = 4
    lazy val cutOffDate: LocalDate = dateService.now().minusMonths(monthLimit)

    customerInfo.customerMigratedToETMPDate.map(LocalDate.parse) match {
      case Some(date) => date.isAfter(cutOffDate)
      case None => false
    }
  }
}
