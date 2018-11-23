/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}

import scala.concurrent.Future

@Singleton
class AuthoriseAsAgentOnly @Inject()(enrolmentsAuthService: EnrolmentsAuthService,
                                     val messagesApi: MessagesApi,
                                     val errorHandler: ErrorHandler,
                                     implicit val appConfig: AppConfig)
  extends AuthBasePredicate with ActionBuilder[Agent] with ActionFunction[Request, Agent] {

  override def invokeBlock[A](request: Request[A], block: Agent[A] => Future[Result]): Future[Result] = {

  implicit val req: Request[A] = request

    enrolmentsAuthService.authorised().retrieve(Retrievals.affinityGroup and Retrievals.allEnrolments) {
      case Some(affinityGroup) ~ allEnrolments =>
        (isAgent(affinityGroup), allEnrolments) match {
          case (true, _) =>
            Logger.debug("[AuthoriseAsAgentOnly][invokeBlock] - Is an Agent, checking HMRC-AS-AGENT enrolment")
            checkAgentEnrolment(allEnrolments, block)
          case (_, _) =>
            Logger.debug("[AuthoriseAsAgentOnly][invokeBlock] - Is NOT an Agent, rendering Technical Difficulties view")
            Future.successful(errorHandler.showInternalServerError)
        }
      case _ =>
        Logger.warn("[AuthoriseAsAgentOnly][invokeBlock] - Missing affinity group")
        Future.successful(errorHandler.showInternalServerError)
    } recover {
      case _: NoActiveSession =>
        Logger.debug("[AuthoriseAsAgentOnly][invokeBlock] - No Active Session, rendering Session Timeout view")
        Unauthorized(views.html.errors.sessionTimeout())
      case _: AuthorisationException =>
        Logger.warn("[AuthoriseAsAgentOnly][invokeBlock] - Authorisation Exception, rendering Technical Difficulties view")
        errorHandler.showInternalServerError
    }
  }

  private def checkAgentEnrolment[A](enrolments: Enrolments, block: Agent[A] => Future[Result])(implicit request: Request[A]) =
    if (enrolments.enrolments.exists(_.key == EnrolmentKeys.agentEnrolmentId)) {
      Logger.debug("[AuthoriseAsAgentOnly][checkAgentEnrolment] - Authenticated as agent")
      block(Agent(enrolments))
    }
    else {
      Logger.debug(s"[AuthoriseAsAgentOnly][checkAgentEnrolment] - Agent without HMRC-AS-AGENT enrolment. Enrolments: $enrolments")
      Logger.warn(s"[AuthoriseAsAgentOnly][checkAgentEnrolment] - Agent without HMRC-AS-AGENT enrolment")
      Future.successful(Forbidden(views.html.errors.agent.unauthorisedNoEnrolment()))
    }
}
