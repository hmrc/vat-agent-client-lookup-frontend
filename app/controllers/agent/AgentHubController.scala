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

import audit.AuditService
import audit.models.AgentOverviewPageViewAuditModel

import javax.inject.{Inject, Singleton}
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import connectors.httpParsers.ResponseHttpParser.HttpResult
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentWithClient
import models.penalties.PenaltiesSummary
import models.{CustomerDetails, HubViewModel, User, VatDetailsDataModel}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CustomerDetailsService, DateService, FinancialDataService, PenaltiesService}
import utils.LoggingUtil
import views.html.agent.AgentHubView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentHubController @Inject()(authenticate: AuthoriseAsAgentWithClient,
                                   serviceErrorHandler: ErrorHandler,
                                   customerDetailsService: CustomerDetailsService,
                                   dateService: DateService,
                                   financialDataService: FinancialDataService,
                                   penaltiesService: PenaltiesService,
                                   auditService: AuditService,
                                   mcc: MessagesControllerComponents,
                                   agentHubView: AgentHubView)
                                  (implicit appConfig: AppConfig,
                                   executionContext: ExecutionContext) extends BaseController(mcc) with LoggingUtil {

  def show: Action[AnyContent] = authenticate.async { implicit user =>
    for {
      customerDetails <- customerDetailsService.getCustomerDetails(user.vrn)
      payments <- if (userIsHybrid(customerDetails)) Future.successful(VatDetailsDataModel(Seq(), isError = false)) else retrievePayments
      penaltiesInformation <- penaltiesService.getPenaltiesInformation(user.vrn)
    } yield {
      customerDetails match {
        case Right(details) =>
          auditService.extendedAudit(AgentOverviewPageViewAuditModel(user, payments), Some(routes.AgentHubController.show.url))
          if (details.missingTrader && !details.hasPendingPPOB) {
            Redirect(appConfig.manageVatMissingTraderUrl)
          } else {
            val optPenaltiesSummary: Option[PenaltiesSummary] = penaltiesInformation.fold(_ => None, Some(_))
            Ok(agentHubView(constructViewModel(details, payments, optPenaltiesSummary)))
          }
        case Left(error) =>
          errorLog(s"[AgentHubController][show] - received an error from CustomerDetailsService: $error")
          serviceErrorHandler.showInternalServerError
      }
    }
  }

  def constructViewModel(details: CustomerDetails, paymentsModel: VatDetailsDataModel,
                         penaltiesInformation: Option[PenaltiesSummary])(implicit user: User[_]): HubViewModel = {

    val hasDDSetup: Option[Boolean] = user.session.get(SessionKeys.mtdVatAgentDDMandateFound) match {
      case Some("true") => Some(true)
      case Some("false") => Some(false)
      case _ => None
    }
    val nextPaymentDate = paymentsModel.payments.headOption.map(payment => payment.dueDate)
    val paymentsNumber  = paymentsModel.payments.length
    val isOverdue       =
      if(paymentsNumber > 1) {
        false
      } else {
        paymentsModel.payments.headOption.fold(false) {
          payment => payment.dueDate.isBefore(dateService.now()) && !payment.ddCollectionInProgress
        }
      }

    HubViewModel(
      details,
      user.vrn,
      dateService.now(),
      nextPaymentDate,
      isOverdue,
      paymentsModel.isError,
      paymentsNumber,
      hasDDSetup,
      penaltiesInformation
    )
  }

  private def retrievePayments(implicit user: User[_]): Future[VatDetailsDataModel] =
    financialDataService.getPayment(user.vrn) map {
      case Right(payments) => VatDetailsDataModel(payments.filterNot(_.chargeType equals "Payment on account"), isError = false)
      case Left(_) => VatDetailsDataModel(Seq(), isError = true)
    }

  private def userIsHybrid(accountDetails: HttpResult[CustomerDetails]): Boolean =
    accountDetails match {
      case Right(model) => model.isHybridUser
      case Left(_) => false
    }
}
