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

import audit.AuditService
import audit.models.{NoPreferenceAuditModel, YesPreferenceAttemptedAuditModel}
import common.SessionKeys
import config.AppConfig
import controllers.BaseController
import controllers.predicates.{AuthoriseAsAgentOnly, PreferencePredicate}
import forms.PreferenceForm._
import javax.inject.{Inject, Singleton}
import models.{PreferenceModel, Yes}
import play.api.mvc._
import views.html.agent.CapturePreferenceView
import scala.concurrent.ExecutionContext

@Singleton
class CapturePreferenceController @Inject()(val authenticate: AuthoriseAsAgentOnly,
                                            val preferenceCheck: PreferencePredicate,
                                            val auditService: AuditService,
                                            mcc: MessagesControllerComponents,
                                            capturePreferenceView: CapturePreferenceView,
                                            implicit val executionContext: ExecutionContext,
                                            implicit val appConfig: AppConfig) extends BaseController(mcc) {

  def show(altRedirectUrl: String = ""): Action[AnyContent] = (authenticate andThen preferenceCheck) { implicit user =>
    val preference = user.session.get(SessionKeys.preference)
    val notificationEmail = user.session.get(SessionKeys.notificationsEmail)
    val clientVrn = user.session.get(SessionKeys.clientVRN)
    val redirectUrl = if(altRedirectUrl.nonEmpty) altRedirectUrl else user.session.get(SessionKeys.redirectUrl).getOrElse("")
    if (clientVrn.isEmpty) {
      Redirect(controllers.agent.routes.SelectClientVrnController.show(redirectUrl))
    } else {
      preference match {
        case Some(_) =>
          Ok(capturePreferenceView(preferenceForm.fill(PreferenceModel(Yes, notificationEmail))))
            .addingToSession(SessionKeys.redirectUrl -> redirectUrl)
        case None =>
          Ok(capturePreferenceView(preferenceForm))
            .addingToSession(SessionKeys.redirectUrl -> redirectUrl)
      }
    }
  }

  def submit: Action[AnyContent] = (authenticate andThen preferenceCheck) { implicit user =>
    preferenceForm.bindFromRequest().fold(
      error => BadRequest(capturePreferenceView(error)),
      formData => {
        if (formData.yesNo.value) {
          auditService.extendedAudit(
            YesPreferenceAttemptedAuditModel(user.arn, formData.email.getOrElse("")),
            Some(controllers.agent.routes.CapturePreferenceController.submit().url)
          )
          Redirect(controllers.agent.routes.ConfirmEmailController.show())
            .addingToSession(SessionKeys.preference -> yes)
            .addingToSession(SessionKeys.notificationsEmail -> formData.email.getOrElse(""))
        } else {
          auditService.extendedAudit(
            NoPreferenceAuditModel(user.arn),
            Some(controllers.agent.routes.CapturePreferenceController.submit().url)
          )

          val redirectUrl = user.session.get(SessionKeys.redirectUrl)
            .getOrElse(appConfig.manageVatCustomerDetailsUrl)
          Redirect(redirectUrl).addingToSession(SessionKeys.preference -> no)
        }
      }
    )
  }
}
