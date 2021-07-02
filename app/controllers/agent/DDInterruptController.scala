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

import common.SessionKeys
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentWithClient
import forms.DDInterruptForm
import models.CustomerDetails
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CustomerDetailsService, DateService, DirectDebitService}
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
                                      directDebitService: DirectDebitService)
                                     (implicit val appConfig: AppConfig,
                                      ec: ExecutionContext) extends BaseController(mcc) {

  def show: Action[AnyContent] = authenticate.async { implicit agent =>
    if(appConfig.features.directDebitInterruptFeature()) {
      customerDetailsService.getCustomerDetails(agent.vrn).flatMap {
        case Right(details) if migratedWithin4M(details) =>
          directDebitService.getDirectDebit(agent.vrn).map {
            case Right(directDebit) if !directDebit.directDebitMandateFound =>
              Ok(ddInterruptView(DDInterruptForm.form))
            case _ => Redirect(routes.ConfirmClientVrnController.redirect().url)
                        .addingToSession(SessionKeys.viewedDDInterrupt -> "true")
          }
        case _ => Future.successful(Redirect(routes.ConfirmClientVrnController.redirect().url)
                    .addingToSession(SessionKeys.viewedDDInterrupt -> "true"))
      }
    } else {
      Future.successful(Redirect(routes.ConfirmClientVrnController.redirect().url)
        .addingToSession(SessionKeys.viewedDDInterrupt -> "true"))
    }
  }

  def submit: Action[AnyContent] = authenticate { implicit agent =>
    (appConfig.features.directDebitInterruptFeature(), agent.session.get(SessionKeys.viewedDDInterrupt).isDefined) match {
      case (true, true) | (false, _) =>
        Redirect(controllers.agent.routes.AgentHubController.show())
      case (true, _) =>
        DDInterruptForm.form.bindFromRequest().fold(
          error => BadRequest(ddInterruptView(error)),
          _ => Redirect(controllers.agent.routes.AgentHubController.show())
                .addingToSession(SessionKeys.viewedDDInterrupt -> "true")
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
