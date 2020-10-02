/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.PasscodeForm
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.EmailVerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.agent.VerifyEmailPinView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailPinController @Inject()(val authenticate: AuthoriseAsAgentOnly,
                                         val preferenceCheck: PreferencePredicate,
                                         val errorHandler: ErrorHandler,
                                         mcc: MessagesControllerComponents,
                                         verifyEmailPinView: VerifyEmailPinView,
                                         implicit val executionContext: ExecutionContext,
                                         implicit val appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = (authenticate andThen preferenceCheck) { implicit agent =>
    if(appConfig.features.emailPinVerificationEnabled()){
      agent.session.get(SessionKeys.notificationsEmail) match {
        case Some(email) => Ok(verifyEmailPinView(email, PasscodeForm.form))
        case _ => Redirect(routes.CapturePreferenceController.show())
      }
    } else {
      NotFound(errorHandler.notFoundTemplate(agent))
    }

  }

  def submit: Action[AnyContent] = (authenticate andThen preferenceCheck).async { implicit agent =>
    if(appConfig.features.emailPinVerificationEnabled()){
      agent.session.get(SessionKeys.notificationsEmail) match {
        case Some(email) =>
          PasscodeForm.form.bindFromRequest().fold(
            error => {
              Logger.debug(s"[VerifyEmailPinController][submit] Error submitting form: $error")
              Future.successful(BadRequest(verifyEmailPinView(email, error)))
            },
            agentEmail => {
              val redirectUrl = agent.session.get(SessionKeys.redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl)
              // TODO - send user's entered passcode to verification service and handle response
              Future.successful(Redirect(redirectUrl).addingToSession(SessionKeys.verifiedAgentEmail -> email))
            }
          )

        case _ => Future.successful(Redirect(routes.CapturePreferenceController.show()))
      }
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate(agent)))
    }

  }

}
