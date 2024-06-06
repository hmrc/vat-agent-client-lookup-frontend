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

import javax.inject.{Inject, Singleton}
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import connectors.httpParsers.VerifyPasscodeHttpParser._
import controllers.BaseController
import controllers.predicates.{AuthoriseAsAgentOnly, PreferencePredicate}
import forms.PasscodeForm
import models.Agent
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.EmailVerificationService
import utils.LoggingUtil
import views.html.agent.VerifyEmailPinView
import views.html.errors.agent.IncorrectPasscodeView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailPinController @Inject()(emailVerificationService: EmailVerificationService,
                                         authenticate: AuthoriseAsAgentOnly,
                                         preferenceCheck: PreferencePredicate,
                                         errorHandler: ErrorHandler,
                                         mcc: MessagesControllerComponents,
                                         verifyEmailPinView: VerifyEmailPinView,
                                         incorrectPasscodeView: IncorrectPasscodeView)
                                        (implicit executionContext: ExecutionContext,
                                         appConfig: AppConfig) extends BaseController(mcc) with LoggingUtil {

  def extractSessionEmail(implicit agent: Agent[AnyContent]): Option[String] =
    agent.session.get(SessionKeys.notificationsEmail).filter(_.nonEmpty)

  def show: Action[AnyContent] = (authenticate andThen preferenceCheck) { implicit agent => {
    extractSessionEmail(agent) match {
      case Some(email) =>
        Ok(verifyEmailPinView(email, PasscodeForm.form))
      case _ =>
        warnLog("[VerifyEmailPinController][show] - could not retrieve email from sessions")
        Redirect(routes.CapturePreferenceController.show())
    }
  }}

  def requestPasscode: Action[AnyContent] = (authenticate andThen preferenceCheck).async { implicit agent => {

    val langCookieValue = agent.cookies.get("PLAY_LANG").map(_.value).getOrElse("en")

    extractSessionEmail(agent) match {
      case Some(email) =>
        emailVerificationService.createEmailPasscodeRequest(email, langCookieValue) map {
          case Some(true) =>
            infoLog("[VerifyEmailPinController][requestPasscode] - the email verification passcode was successfully sent")
            Redirect(routes.VerifyEmailPinController.show)
          case Some(false) =>
            warnLog(
              "[VerifyEmailPinController][requestPasscode] - " +
                "Unable to send email verification request. Service responded with 'already verified'"
            )
            Redirect(agent.session.get(SessionKeys.redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl))
              .addingToSession(SessionKeys.verifiedEmail -> email)
          case _ =>
            errorLog("[VerifyEmailPinController][requestPasscode] - an unexpected error occured while sending a verification passcode to the email")
            errorHandler.showInternalServerError
        }
      case _ =>
        warnLog("[VerifyEmailPinController][requestPasscode] - could not retrieve email from session")
        Future.successful(Redirect(routes.CapturePreferenceController.show()))
    }
  }}

  def submit: Action[AnyContent] = (authenticate andThen preferenceCheck).async { implicit agent => {
    extractSessionEmail(agent) match {
      case Some(email) =>
        PasscodeForm.form.bindFromRequest().fold(
          error => {
            warnLog(s"[VerifyEmailPinController][submit] Error submitting form: $error")
            Future.successful(BadRequest(verifyEmailPinView(email, error)))
          },
          passcode => {
            emailVerificationService.verifyPasscode(email, passcode).map {
              case Right(SuccessfullyVerified) | Right(AlreadyVerified) =>
                infoLog("[VerifyEmailPinController][submit] - successfully verified email address")
                Redirect(agent.session.get(SessionKeys.redirectUrl).getOrElse(appConfig.manageVatCustomerDetailsUrl))
                  .addingToSession(SessionKeys.verifiedEmail -> email)
              case Right(TooManyAttempts) =>
                warnLog("[VerifyEmailPinController][submit] - failed to verify the email address; the user attempted to verify the email with the same code multiple times")
                BadRequest(incorrectPasscodeView("incorrectPasscode.tooManyAttempts"))
              case Right(PasscodeNotFound) =>
                warnLog("[VerifyEmailPinController][submit] - failed to verify the email address; the verification code has expired")
                BadRequest(incorrectPasscodeView("incorrectPasscode.expired"))
              case Right(IncorrectPasscode) =>
                warnLog("[VerifyEmailPinController][submit] - failed to verify the email address; the verification code is incorrect")
                BadRequest(verifyEmailPinView(
                  email,
                  PasscodeForm.form.withError("passcode", "passcode.error.invalid")
                ))
              case _ =>
                errorLog("[VerifyEmailPinController][submit] - an unexpected error was received while attempting to verify the email address")
                errorHandler.showInternalServerError
            }
          }
        )
      case _ =>
        errorLog("[VerifyEmailPinController][requestPasscode] - could not retrieve email from session")
        Future.successful(Redirect(routes.CapturePreferenceController.show()))
    }
  }}

}
