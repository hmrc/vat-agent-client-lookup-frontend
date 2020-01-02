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
import play.api.i18n.MessagesApi
import play.api.mvc._

@Singleton
class CapturePreferenceController @Inject()(val messagesApi: MessagesApi,
                                            val authenticate: AuthoriseAsAgentOnly,
                                            val preferenceCheck: PreferencePredicate,
                                            val auditService: AuditService,
                                            implicit val appConfig: AppConfig) extends BaseController {

  def show: Action[AnyContent] = (authenticate andThen preferenceCheck) { implicit user =>
    val preference = user.session.get(SessionKeys.preference)
    val notificationEmail = user.session.get(SessionKeys.notificationsEmail)
    val clientVrn = user.session.get(SessionKeys.clientVRN)
    val redirectUrl = user.session.get(SessionKeys.redirectUrl).getOrElse("")

    if (clientVrn.isEmpty) {
      Redirect(controllers.agent.routes.SelectClientVrnController.show(redirectUrl))
    } else {
      preference match {
        case Some(_) =>
          Ok(views.html.agent.capturePreference(preferenceForm.fill(PreferenceModel(Yes, notificationEmail))))
        case None =>
          Ok(views.html.agent.capturePreference(preferenceForm))
      }
    }
  }

  def submit: Action[AnyContent] = (authenticate andThen preferenceCheck) { implicit user =>
    preferenceForm.bindFromRequest().fold(
      error => BadRequest(views.html.agent.capturePreference(error)),
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
