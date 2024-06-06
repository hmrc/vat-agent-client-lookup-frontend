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

package testOnly.controllers

import javax.inject.Inject
import config.AppConfig
import controllers.BaseController
import forms.FeatureSwitchForm
import models.FeatureSwitchModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import testOnly.views.html.featureSwitch.FeatureSwitch

class FeatureSwitchController @Inject()(featureSwitchView: FeatureSwitch, mcc: MessagesControllerComponents)
                                       (implicit appConfig: AppConfig) extends BaseController(mcc) {

  def featureSwitch: Action[AnyContent] = Action { implicit request =>
    Ok(featureSwitchView(FeatureSwitchForm.form.fill(
      FeatureSwitchModel(
        emailVerificationEnabled = appConfig.features.emailVerificationEnabled(),
        useStaticDateFeature = appConfig.features.useStaticDateFeature()
      )
    )))
  }

  def submitFeatureSwitch: Action[AnyContent] = Action { implicit request =>
    FeatureSwitchForm.form.bindFromRequest().fold(
      _ => Redirect(routes.FeatureSwitchController.featureSwitch),
      success = handleSuccess
    )
  }

  def handleSuccess(model: FeatureSwitchModel): Result = {
    appConfig.features.emailVerificationEnabled(model.emailVerificationEnabled)
    appConfig.features.useStaticDateFeature(model.useStaticDateFeature)
    Redirect(routes.FeatureSwitchController.featureSwitch)
  }
}
