/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.predicates

import javax.inject.{Inject, Singleton}
import audit.AuditService
import common.{EnrolmentKeys, SessionKeys}
import config.{AppConfig, ErrorHandler}
import models.{Agent, User}
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core._
import services.EnrolmentsAuthService
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.LoggingUtil
import views.html.errors.SessionTimeoutView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthoriseAsAgentWithClient @Inject()(enrolmentsAuthService: EnrolmentsAuthService,
                                           val auditService: AuditService,
                                           val serviceErrorHandler: ErrorHandler,
                                           mcc: MessagesControllerComponents,
                                           sessionTimeoutView: SessionTimeoutView,
                                           implicit val appConfig: AppConfig,
                                           override implicit val executionContext: ExecutionContext)
  extends AuthBasePredicate(mcc) with ActionBuilder[User, AnyContent] with ActionFunction[Request, User] with LoggingUtil{

  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  private def delegatedAuthRule(vrn: String): Enrolment =
    Enrolment(EnrolmentKeys.vatEnrolmentId)
      .withIdentifier(EnrolmentKeys.vatIdentifierId, vrn)
      .withDelegatedAuthRule(EnrolmentKeys.mtdVatDelegatedAuthRule)

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {
    implicit val req: Request[A] = request

    request.session.get(SessionKeys.clientVRN) match {
      case Some(vrn) =>
        debug(s"[AuthoriseAsAgentWithClient][invokeBlock] - Client VRN from Session: $vrn")
        enrolmentsAuthService.authorised(delegatedAuthRule(vrn)).retrieve(Retrievals.affinityGroup and Retrievals.allEnrolments) {
          case None ~ _ =>
            errorLog("[][] - no enrolment found")
            Future.successful(serviceErrorHandler.showInternalServerError)
          case _ ~ allEnrolments =>
            val agent = Agent(allEnrolments)
            val user = User(vrn, active = true, Some(agent.arn))
            block(user)
        } recover {
          case _: NoActiveSession =>
            warnLog("[AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have an active session, rendering Session Timeout")
            Unauthorized(sessionTimeoutView())
          case _: AuthorisationException =>
            errorLog("[AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have delegated authority for Client")
            Redirect(controllers.agent.routes.AgentUnauthorisedForClientController.show())
          case error: UpstreamErrorResponse =>
            errorLog(s"[AuthoriseAsAgentWithClient][invokeBlock] - Upstream error response received: ${error.message}")
            serviceErrorHandler.showInternalServerError
        }
      case _ =>
        warnLog("[AuthoriseAsAgentWithClient][invokeBlock] - No Client VRN in session, redirecting to Select Client page")
        Future.successful(Redirect(controllers.agent.routes.SelectClientVrnController.show()))
    }
  }
}
