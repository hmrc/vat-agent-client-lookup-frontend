/*
 * Copyright 2019 HM Revenue & Customs
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
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthoriseAsAgentOnly, PreferencePredicate}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.EmailVerificationService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class VerifyEmailController @Inject()(val authenticate: AuthoriseAsAgentOnly,
                                      val preferenceCheck: PreferencePredicate,
                                      val messagesApi: MessagesApi,
                                      val emailVerificationService: EmailVerificationService,
                                      val errorHandler: ErrorHandler,
                                      implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def show: Action[AnyContent] = (authenticate andThen preferenceCheck) { implicit agent =>

    agent.session.get(SessionKeys.notificationsEmail) match {
      case Some(email) => Ok(views.html.agent.verifyEmail(email))
      case _ => Redirect(routes.CapturePreferenceController.show())
    }
  }

  def sendVerification: Action[AnyContent] = (authenticate andThen preferenceCheck).async { implicit agent =>

    agent.session.get(SessionKeys.notificationsEmail) match {
      case Some(email) => emailVerificationService.createEmailVerificationRequest(
        email, routes.ConfirmEmailController.isEmailVerified().url) map {
          case Some(true) => Redirect(routes.VerifyEmailController.show())
          case Some(false) =>
            Logger.warn(
              "[VerifyEmailController][sendVerification] - " +
                "Unable to send email verification request. Service responded with 'already verified'"
            )

            Redirect(agent.session.get(SessionKeys.redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl))

          case _ => errorHandler.showInternalServerError
        }

      case _ => Future.successful(Redirect(routes.CapturePreferenceController.show()))
    }
  }
}
