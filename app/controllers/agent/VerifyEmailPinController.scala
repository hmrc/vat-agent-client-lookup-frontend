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
import connectors.httpParsers.VerifyPasscodeHttpParser._
import controllers.predicates.{AuthoriseAsAgentOnly, PreferencePredicate}
import forms.PasscodeForm
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.EmailVerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.agent.VerifyEmailPinView
import views.html.errors.agent.IncorrectPasscodeView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailPinController @Inject()(emailVerificationService: EmailVerificationService,
                                         authenticate: AuthoriseAsAgentOnly,
                                         val preferenceCheck: PreferencePredicate,
                                         val errorHandler: ErrorHandler,
                                         mcc: MessagesControllerComponents,
                                         verifyEmailPinView: VerifyEmailPinView,
                                         incorrectPasscodeView: IncorrectPasscodeView,
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

  def requestPasscode: Action[AnyContent] = (authenticate andThen preferenceCheck).async { implicit agent =>
    if(appConfig.features.emailPinVerificationEnabled()){

      val langCookieValue = agent.cookies.get("PLAY_LANG").map(_.value).getOrElse("en")

      agent.session.get(SessionKeys.notificationsEmail) match {
        case Some(email) =>
          emailVerificationService.createEmailPasscodeRequest(email, langCookieValue) map {
            case Some(true) => Redirect(routes.VerifyEmailPinController.show())
            case Some(false) =>
              Logger.debug(
                "[VerifyEmailController][sendVerification] - " +
                  "Unable to send email verification request. Service responded with 'already verified'"
              )
              Redirect(agent.session.get(SessionKeys.redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl))
                .addingToSession(SessionKeys.verifiedAgentEmail -> email)
            case _ =>  errorHandler.showInternalServerError
          }
        case _ => Future.successful(Redirect(routes.CapturePreferenceController.show()))
      }
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate(agent)))
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
            passcode => {
              emailVerificationService.verifyPasscode(email, passcode).map{
                case Right(SuccessfullyVerified) | Right(AlreadyVerified) =>
                  Redirect(agent.session.get(SessionKeys.redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl))
                    .addingToSession(SessionKeys.verifiedAgentEmail -> email)
                case Right(TooManyAttempts) => BadRequest(incorrectPasscodeView("incorrectPasscode.tooManyAttempts"))
                case Right(PasscodeNotFound) => BadRequest(incorrectPasscodeView("incorrectPasscode.expired"))
                case Right(IncorrectPasscode) =>
                  BadRequest(verifyEmailPinView(
                    email,
                    PasscodeForm.form.withError("passcode", "passcode.error.invalid")
                  ))
                case _ => errorHandler.showInternalServerError
              }
            }
          )
        case _ => Future.successful(Redirect(routes.CapturePreferenceController.show()))
      }
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate(agent)))
    }
  }

}
