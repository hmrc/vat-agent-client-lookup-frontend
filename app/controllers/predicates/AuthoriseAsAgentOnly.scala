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

import common.EnrolmentKeys
import config.{AppConfig, ErrorHandler}

import javax.inject.{Inject, Singleton}
import models.Agent
import play.api.mvc._
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolments, NoActiveSession}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.LoggingUtil
import views.html.errors.SessionTimeoutView
import views.html.errors.agent.UnauthorisedNoEnrolmentView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthoriseAsAgentOnly @Inject()(enrolmentsAuthService: EnrolmentsAuthService,
                                     val errorHandler: ErrorHandler,
                                     mcc: MessagesControllerComponents,
                                     unauthorisedNoEnrolmentView: UnauthorisedNoEnrolmentView,
                                     sessionTimeoutView: SessionTimeoutView,
                                     implicit val appConfig: AppConfig)
  extends AuthBasePredicate(mcc) with ActionBuilder[Agent, AnyContent] with ActionFunction[Request, Agent] with LoggingUtil {

  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser
  override implicit protected val executionContext: ExecutionContext = mcc.executionContext

  override def invokeBlock[A](request: Request[A], block: Agent[A] => Future[Result]): Future[Result] = {

  implicit val req: Request[A] = request

    enrolmentsAuthService.authorised().retrieve(affinityGroup and allEnrolments) {
      case Some(affinityGroup) ~ allEnrolments =>
        (isAgent(affinityGroup), allEnrolments) match {
          case (true, _) =>
            infoLog("[AuthoriseAsAgentOnly][invokeBlock] - Is an Agent, checking HMRC-AS-AGENT enrolment")
            checkAgentEnrolment(allEnrolments, block)
          case (_, _) =>
            errorLog("[AuthoriseAsAgentOnly][invokeBlock] - Is NOT an Agent, rendering Technical Difficulties view")
            Future.successful(errorHandler.showInternalServerError)
        }
      case _ =>
        errorLog("[AuthoriseAsAgentOnly][invokeBlock] - Missing affinity group")
        Future.successful(errorHandler.showInternalServerError)
    } recover {
      case _: NoActiveSession =>
        errorLog("[AuthoriseAsAgentOnly][invokeBlock] - No Active Session, rendering Session Timeout view")
        Unauthorized(sessionTimeoutView())
      case _: AuthorisationException =>
        errorLog("[AuthoriseAsAgentOnly][invokeBlock] - Authorisation Exception, rendering Technical Difficulties view")
        errorHandler.showInternalServerError
      case error: UpstreamErrorResponse =>
        errorLog(s"[AuthoriseAsAgentOnly][invokeBlock] - Upstream error response received: ${error.message}")
        errorHandler.showInternalServerError
    }
  }

  private def checkAgentEnrolment[A](enrolments: Enrolments, block: Agent[A] => Future[Result])(implicit request: Request[A]) =
    if (enrolments.enrolments.exists(_.key == EnrolmentKeys.agentEnrolmentId)) {
      debug("[AuthoriseAsAgentOnly][checkAgentEnrolment] - Authenticated as agent")
      block(Agent(enrolments))
    }
    else {
      errorLog(s"[AuthoriseAsAgentOnly][checkAgentEnrolment] - Agent without HMRC-AS-AGENT enrolment. Enrolments: $enrolments")
      Future.successful(Forbidden(unauthorisedNoEnrolmentView()))
    }
}
