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
import common.SessionKeys
import config.AppConfig
import models.Agent
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PreferencePredicate @Inject()(implicit val appConfig: AppConfig,
                                    override val executionContext: ExecutionContext) extends ActionRefiner[Agent, Agent] {

  override def refine[A](request: Agent[A]): Future[Either[Result, Agent[A]]] = {

    implicit val agent: Agent[A] = request

    val redirectUrl = agent.session.get(SessionKeys.redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl)
    val preference = agent.session.get(SessionKeys.preference)
    val hasVerifiedEmail = agent.session.get(SessionKeys.verifiedEmail).isDefined

      preference match {
        case Some("no") => Future.successful(Left(Redirect(redirectUrl)))
        case _ if hasVerifiedEmail => Future.successful(Left(Redirect(redirectUrl)))
        case _ => Future.successful(Right(agent))
      }
  }
}
