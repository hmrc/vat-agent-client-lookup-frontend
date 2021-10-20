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

import javax.inject.{Inject, Singleton}
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import connectors.httpParsers.ResponseHttpParser.HttpResult
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentWithClient
import models.{Charge, CustomerDetails, HubViewModel, User}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CustomerDetailsService, DateService, FinancialDataService}
import views.html.agent.AgentHubView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentHubController @Inject()(authenticate: AuthoriseAsAgentWithClient,
                                   serviceErrorHandler: ErrorHandler,
                                   customerDetailsService: CustomerDetailsService,
                                   dateService: DateService,
                                   financialDataService: FinancialDataService,
                                   mcc: MessagesControllerComponents,
                                   agentHubView: AgentHubView)
                                  (implicit appConfig: AppConfig,
                                   executionContext: ExecutionContext) extends BaseController(mcc) {

  def show: Action[AnyContent] = authenticate.async { implicit user =>
    for {
      customerDetails <- customerDetailsService.getCustomerDetails(user.vrn)
      payments <- if (userIsHybrid(customerDetails)) Future.successful(Seq()) else retrievePayments
    } yield {
      customerDetails match {
        case Right(details) =>
          if (details.missingTrader && !details.hasPendingPPOB) {
            Redirect(appConfig.manageVatMissingTraderUrl)
          } else {
            if(details.deregistration.isDefined && details.deregistration.flatMap(_.effectDateOfCancellation).isEmpty) {
              logger.warn("[AgentHubController][show] - 'deregistration' contained no 'effectDateOfCancellation'")
            }
            Ok(agentHubView(constructViewModel(details, payments)))
          }
        case Left(error) =>
          logger.warn(s"[AgentHubController][show] - received an error from CustomerDetailsService: $error")
          serviceErrorHandler.showInternalServerError
      }
    }
  }

  def constructViewModel(details: CustomerDetails, payments: Seq[Charge])(implicit user: User[_]): HubViewModel = {

    val showBlueBox: Boolean = user.session.get(SessionKeys.viewedDDInterrupt).contains("blueBox")
    val nextPaymentDate = payments.headOption.map(payment => payment.dueDate)
    val paymentsNumber  = payments.length
    val isOverdue       =
      if(paymentsNumber > 1) {
        false
      } else {
        payments.headOption.fold(false) {
          payment => payment.dueDate.isBefore(dateService.now()) && !payment.ddCollectionInProgress
        }
      }

    HubViewModel(
      details,
      user.vrn,
      dateService.now(),
      showBlueBox,
      nextPaymentDate,
      isOverdue,
      paymentsNumber
    )
  }

  private def retrievePayments(implicit user: User[_]): Future[Seq[Charge]] =
    financialDataService.getPayment(user.vrn) map {
      case Right(payments) => payments
      case Left (_) => Seq()
    }

  private def userIsHybrid(accountDetails: HttpResult[CustomerDetails]): Boolean =
    accountDetails match {
      case Right(model) => model.isHybridUser
      case Left(_) => false
    }
}
