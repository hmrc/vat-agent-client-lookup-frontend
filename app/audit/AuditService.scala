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

package audit

import javax.inject.{Inject, Singleton}
import audit.models.ExtendedAuditModel
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.LoggingUtil

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(auditConnector: AuditConnector) extends LoggingUtil {

  val appName: String = "vat-agent-client-lookup-frontend"

  val referrer: HeaderCarrier => String = _.extraHeaders.find(_._1 == HeaderNames.REFERER).map(_._2).getOrElse("-")

  def extendedAudit(auditModel: ExtendedAuditModel, path: Option[String] = None)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Unit = {
    val extendedDataEvent = toExtendedDataEvent(appName, auditModel, path.fold(referrer(hc))(x => x))
    debug(s"Splunk Audit Event:\n\n $extendedDataEvent")
    handleAuditResult(auditConnector.sendExtendedEvent(extendedDataEvent))
  }

  def toExtendedDataEvent(appName: String, auditModel: ExtendedAuditModel, path: String)
                         (implicit hc: HeaderCarrier): ExtendedDataEvent = {

    val details: JsValue =
      Json.toJson(AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()).as[JsObject].deepMerge(auditModel.detail.as[JsObject])

    ExtendedDataEvent(
      auditSource = appName,
      auditType = auditModel.auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(auditModel.transactionName, path),
      detail = details
    )
  }

  private def handleAuditResult(auditResult: Future[AuditResult])(implicit ec: ExecutionContext, request: Request[_]): Unit = auditResult.map {
    case Success =>
      debug("Splunk Audit Successful")
    case Failure(err, _) =>
      errorLog(s"Splunk Audit Error, message: $err")
    case Disabled =>
      debug(s"Auditing Disabled")
  }
}
