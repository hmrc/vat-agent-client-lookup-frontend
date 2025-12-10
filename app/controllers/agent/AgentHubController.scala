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
import models.{CustomerDetails, HubViewModel, StandingRequest, User, VatDetailsDataModel}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CustomerDetailsService, DateService, FinancialDataService, POACheckService, PaymentsOnAccountService, PenaltiesService, AnnualAccountingCheckService}
import utils.LoggingUtil
import views.html.agent.AgentHubView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class AgentHubController @Inject()(authenticate: AuthoriseAsAgentWithClient,
                                   serviceErrorHandler: ErrorHandler,
                                   customerDetailsService: CustomerDetailsService,
                                   dateService: DateService,
                                   financialDataService: FinancialDataService,
                                   penaltiesService: PenaltiesService,
                                   auditService: AuditService,
                                   mcc: MessagesControllerComponents,
                                   agentHubView: AgentHubView,
                                   paymentsOnAccountService: PaymentsOnAccountService,
                                   poaCheckService: POACheckService,
                                   annualAccountingCheckService: AnnualAccountingCheckService)
                                  (implicit appConfig: AppConfig,
                                   executionContext: ExecutionContext) extends BaseController(mcc) with LoggingUtil {

  def show: Action[AnyContent] = authenticate.async { implicit user =>
    for {
      customerDetails <- customerDetailsService.getCustomerDetails(user.vrn)
      payments <- {
        val hybrid = userIsHybrid(customerDetails)
        logger.info(s"[AgentHubController][show] userIsHybrid=$hybrid for vrn=${user.vrn}")
        if (hybrid) {
          logger.info(s"[AgentHubController][show] Skipping retrievePayments due to hybrid user")
          Future.successful(VatDetailsDataModel(Seq(), isError = false))
        } else {
          logger.info(s"[AgentHubController][show] Calling retrievePayments for vrn=${user.vrn}")
          retrievePayments
        }
      }
      penaltiesInformation <- {
        logger.info(s"[AgentHubController][show] Fetching penalties for vrn=${user.vrn}")
        penaltiesService.getPenaltiesInformation(user.vrn)
      }
      standingRequest <-
        {
          val poaFlag = appConfig.features.poaActiveFeature()
          val aaFlag  = appConfig.features.annualAccountingFeature()
          logger.info(s"[AgentHubController][show] Feature flags: poa=$poaFlag annualAccounting=$aaFlag")
          if (poaFlag || aaFlag) {
            logger.info(s"[AgentHubController][show] Fetching standing requests for vrn=${user.vrn}")
            paymentsOnAccountService.getPaymentsOnAccounts(user.vrn)
          } else {
            logger.info(s"[AgentHubController][show] Skipping standing requests fetch (flags off)")
            Future.successful(None)
          }
        }
    } yield {
      customerDetails match {
        case Right(details) =>
          auditService.extendedAudit(AgentOverviewPageViewAuditModel(user, payments), Some(routes.AgentHubController.show.url))
          if (details.missingTrader && !details.hasPendingPPOB) {
            Redirect(appConfig.manageVatMissingTraderUrl)
          } else {
            val optPenaltiesSummary: Option[PenaltiesSummary] = penaltiesInformation.fold(_ => None, Some(_))
            Ok(agentHubView(constructViewModel(details, payments, optPenaltiesSummary, standingRequest)))
          }
        case Left(error) =>
          errorLog(s"[AgentHubController][show] - received an error from CustomerDetailsService: $error")
          serviceErrorHandler.showInternalServerError
      }
    }
  }

  private def isPoaActiveUser(customerInfo: HttpResult[CustomerDetails]) = {
    appConfig.features.poaActiveFeature() && retrievePoaActiveForCustomer(customerInfo)
  }

  def constructViewModel(details: CustomerDetails, paymentsModel: VatDetailsDataModel,
                         penaltiesInformation: Option[PenaltiesSummary], standingRequest: Option[StandingRequest])(implicit user: User[_]): HubViewModel = {

    val hasDDSetup: Option[Boolean] = user.session.get(SessionKeys.mtdVatAgentDDMandateFound) match {
      case Some("true") => Some(true)
      case Some("false") => Some(false)
      case _ => None
    }

    logger.info(s"[AgentHubController][constructViewModel] Payments count=${paymentsModel.payments.size}")
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
    val isPoaActiveForCustomer: Boolean = retrievePoaActiveForCustomer(Right(details))
    val now: LocalDate = dateService.now()
    val poaChangedOn: Option[LocalDate] = poaCheckService.changedOnDateWithInLatestVatPeriod(standingRequest, now)
    val isAnnualAccountingCustomer: Boolean = {
      val hasType4 = standingRequest.exists(_.standingRequests.exists(_.requestCategory == models.ChangedOnVatPeriod.RequestCategoryType4))
      val hasYPeriod = standingRequest.exists(_.standingRequests.exists(_.requestItems.exists(_.periodKey.startsWith("Y"))))
      hasType4 || hasYPeriod
    }
    // Annual Accounting overdue: cross-check AA due dates with transactions when available; else use schedule dates only
    val aaDueDates: Set[LocalDate] = {
      val fromType4 = standingRequest.toSeq
        .flatMap(_.standingRequests.filter(_.requestCategory == models.ChangedOnVatPeriod.RequestCategoryType4))
        .flatMap(_.requestItems.flatMap(ri => Try(LocalDate.parse(ri.dueDate)).toOption ++ ri.postingDueDate.flatMap(d => Try(LocalDate.parse(d)).toOption)))
      val fromYPeriods = standingRequest.toSeq
        .flatMap(_.standingRequests)
        .flatMap(_.requestItems.filter(_.periodKey.startsWith("Y")))
        .flatMap(ri => Try(LocalDate.parse(ri.dueDate)).toOption ++ ri.postingDueDate.flatMap(d => Try(LocalDate.parse(d)).toOption))
      (fromType4 ++ fromYPeriods).toSet
    }
    val aaOverdueFromTxns: Boolean =
      paymentsModel.payments.exists(ch =>
        aaDueDates.contains(ch.dueDate) && ch.dueDate.isBefore(now) && ch.outstandingAmount > 0 && !ch.ddCollectionInProgress
      )
    val aaDatesBeforeNow = aaDueDates.filter(_.isBefore(now)).toSeq.sorted
    logger.info(s"[AgentHubController][constructViewModel] now=$now, aaDatesBeforeNowCount=${aaDatesBeforeNow.size}, firstBeforeNow=${aaDatesBeforeNow.headOption}")
    val isAnnualAccountingPaymentOverdue: Boolean =
      if (paymentsModel.payments.nonEmpty) aaOverdueFromTxns else aaDueDates.exists(_.isBefore(now))

    val annualAccountingChangedOn: Option[LocalDate] =
      if (appConfig.features.annualAccountingFeature() && isAnnualAccountingCustomer)
        annualAccountingCheckService.changedOnDateWithinLast3Months(standingRequest, now)
      else None

    logger.info(s"[AgentHubController][constructViewModel] isPoaActiveForCustomer=$isPoaActiveForCustomer, poaChangedOn=${poaChangedOn.map(_.toString)}")
    logger.info(s"[AgentHubController][constructViewModel] isAnnualAccountingCustomer=$isAnnualAccountingCustomer, aaDueDates=${aaDueDates.size}, aaOverdueFromTxns=$aaOverdueFromTxns, isAAPaymentOverdue=$isAnnualAccountingPaymentOverdue, aaChangedOn=${annualAccountingChangedOn.map(_.toString)}")

    HubViewModel(
      details,
      user.vrn,
      now,
      nextPaymentDate,
      isOverdue,
      paymentsModel.isError,
      paymentsNumber,
      hasDDSetup,
      penaltiesInformation,
      isAnnualAccountingCustomer = isAnnualAccountingCustomer,
      isAnnualAccountingPaymentOverdue = isAnnualAccountingPaymentOverdue,
      annualAccountingChangedOn = annualAccountingChangedOn,
      isPoaActiveForCustomer,
      poaChangedOn
    )
  }

  private def retrievePayments(implicit user: User[_]): Future[VatDetailsDataModel] = {
    logger.info(s"[AgentHubController][retrievePayments] Fetching payments due for vrn=${user.vrn}")
    financialDataService.getPayment(user.vrn) map {
      case Right(payments) =>
        val filtered = payments.filterNot(_.chargeType equals "Payment on account")
        logger.info(s"[AgentHubController][retrievePayments] Retrieved payments=${payments.size}, after filter=${filtered.size}")
        VatDetailsDataModel(filtered, isError = false)
      case Left(err) =>
        logger.warn(s"[AgentHubController][retrievePayments] Error retrieving payments: $err")
        VatDetailsDataModel(Seq(), isError = true)
    }
  }


  private def userIsHybrid(accountDetails: HttpResult[CustomerDetails]): Boolean =
    accountDetails match {
      case Right(model) => model.isHybridUser
      case Left(_) => false
    }

  def retrievePoaActiveForCustomer(accountDetails: HttpResult[CustomerDetails]): Boolean = {
    accountDetails match {
      case Right(customerDetails) => isDateEqualsTodayFuture(customerDetails.poaActiveUntil,dateService.now())
      case _ => false
    }
  }

  val dateFormat: String           = "yyyy-MM-dd"
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
  private def isDateEqualsTodayFuture(poaActiveUntil: Option[String], currentDate: LocalDate): Boolean = {
    poaActiveUntil match {
      case Some(poaActiveUntilDate) =>
        val parsedDate = Try(LocalDate.parse(poaActiveUntilDate, formatter)).getOrElse(LocalDate.MIN)
        if (parsedDate.isAfter(currentDate) || parsedDate.isEqual(currentDate)) {
          logger.info(s"Date condition met, parsedDate ($parsedDate) is today or in the future")
          true
        } else {
          logger.info(s"Date condition failed, parsedDate ($parsedDate) is in the past or not available")
          false
        }
      case _ => false
    }
  }
}
