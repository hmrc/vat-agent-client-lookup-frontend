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

package controllers.predicates

import common.SessionKeys
import controllers.Assets.Redirect
import models.User
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DDInterruptPredicate @Inject()(override val executionContext: ExecutionContext) extends ActionRefiner[User, User] {

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] =
    if(request.session.get(SessionKeys.viewedDDInterrupt).isDefined) {
      Future.successful(Right(request))
    } else {
      Future.successful(Left(Redirect(controllers.agent.routes.DDInterruptController.show().url)))
    }
}
