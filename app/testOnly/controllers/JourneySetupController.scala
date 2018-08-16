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

package testOnly.controllers

import common.SessionKeys
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.Future

@Singleton
class JourneySetupController @Inject()(val messagesApi: MessagesApi, implicit val appConfig: AppConfig, http: HttpClient)
  extends FrontendController {

  def journeySetup(): Action[AnyContent] = Action.async {

    implicit request =>
      Future.successful(Ok("Received and stored the redirect url")) map {
        res => {
          res.addingToSession(SessionKeys.redirectUrl -> appConfig.manageVatBase)
        }
      }
  }
}
