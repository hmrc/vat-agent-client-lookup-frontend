/*
 * Copyright 2023 HM Revenue & Customs
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

package helpers

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.data.Form
import play.api.http.HeaderNames
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.{Application, Environment, Mode}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.AuthStub

import scala.concurrent.ExecutionContext

trait IntegrationBaseSpec extends TestSuite with CustomMatchers with GuiceOneServerPerSuite with ScalaFutures with
  IntegrationPatience with WireMockHelper with BeforeAndAfterEach with BeforeAndAfterAll with Eventually {

  val mockHost: String = WireMockHelper.host
  val mockPort: String = WireMockHelper.wmPort.toString
  val appContextRoute: String = "/vat-through-software/representative"
  val authToken : String = "authToken"

  lazy val mockAppConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(Lang("en-GB"), messagesApi)

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  def authSession: Map[String, String] = Map(authToken -> "mock-bearer-token")

  override def beforeEach(): Unit = {
    mockAppConfig.features.emailVerificationEnabled(true)
    mockAppConfig.features.poaActiveFeature(true)
    super.beforeEach()
  }

  class PreconditionBuilder {
    implicit val builder: PreconditionBuilder = this

    def user: User = new User()
    def agent: Agent = new Agent()
  }

  def `given`: PreconditionBuilder = new PreconditionBuilder

  class User()(implicit builder: PreconditionBuilder) {
    def isAuthenticated: PreconditionBuilder = {
      Given("I stub a User who successfully signed up to MTD VAT")
      AuthStub.authorised()
      builder
    }

    def isNotAuthenticated: PreconditionBuilder = {
      Given("I stub a User who is not logged in")
      AuthStub.unauthorisedNotLoggedIn()
      builder
    }

    def isNotEnrolled: PreconditionBuilder = {
      Given("I stub a User who is NOT signed up to MTD VAT")
      AuthStub.unauthorisedOtherEnrolment()
      builder
    }

    def noAffinityGroup: PreconditionBuilder = {
      Given("I stub a User who is authenticated but does NOT have an Affinity Group")
      AuthStub.authorisedNoAffinityGroup()
      builder
    }
  }

  class Agent()(implicit builder: PreconditionBuilder) {
    def isSignedUpToAgentServices: PreconditionBuilder = {
      Given("I stub an Agent successfully signed up to Agent Services")
      AuthStub.agentAuthorised()
      builder
    }

    def isNotSignedUpToAgentServices: PreconditionBuilder = {
      Given("I stub an Agent who is NOT signed up to Agent Services")
      AuthStub.agentUnauthorisedOtherEnrolment()
      builder
    }

    def isUnauthorised: PreconditionBuilder = {
      Given("I stub an Agent who is Unauthorised")
      AuthStub.insufficientEnrolments()
      builder
    }
  }

  def servicesConfig: Map[String, String] = Map(
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.email-verification.host" -> mockHost,
    "microservice.services.email-verification.port" -> mockPort,
    "microservice.services.financial-transactions.host" -> mockHost,
    "microservice.services.financial-transactions.port" -> mockPort,
    "microservice.services.manage-vat-subscription-frontend.host" -> mockHost,
    "microservice.services.manage-vat-subscription-frontend.port" -> mockPort,
    "microservice.services.manage-vat-subscription-frontend.endpoints.customer-details" -> "/customer-details?isAgent=true",
    "microservice.services.vat-subscription.host" -> mockHost,
    "microservice.services.vat-subscription.port" -> mockPort,
    "penalties.host" -> s"http://$mockHost:$mockPort",
    "features.emailVerification.enabled" -> "true",
    "features.poaStandingSchedule.enabled" -> "true"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .build()

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWireMock()
  }

  override def afterAll(): Unit = {
    stopWireMock()
    super.afterAll()
  }

  def get(path: String, additionalCookies: Map[String, String] = Map.empty): WSResponse = await(
    buildRequest(path, additionalCookies ++ authSession).get()
  )

  def post(path: String, additionalCookies: Map[String, String] = Map.empty)(body: Map[String, Seq[String]]): WSResponse = await(
    buildRequest(path, additionalCookies ++ authSession).post(body)
  )

  def postJSValueBody(path: String, additionalCookies: Map[String, String] = Map.empty)(body: JsValue): WSResponse = await(
    buildRequest(path, additionalCookies).post(body)
  )

  def buildRequest(path: String, additionalCookies: Map[String, String] = Map.empty): WSRequest =
    client.url(s"http://localhost:$port$appContextRoute$path")
      .withHttpHeaders(HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(additionalCookies), "Csrf-Token" -> "nocheck")
      .withFollowRedirects(false)

  def document(response: WSResponse): Document = Jsoup.parse(response.body)

  def redirectLocation(response: WSResponse): Option[String] = response.header(HeaderNames.LOCATION)

  def toFormData[T](form: Form[T], data: T): Map[String, Seq[String]] =
    form.fill(data).data map { case (k, v) => k -> Seq(v) }

}
