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

package controllers.predicates

import assets.BaseTestConstants._
import common.SessionKeys
import controllers.ControllerBaseSpec
import models.Agent
import org.scalatest.BeforeAndAfterAll
import play.api.mvc.Results.Redirect

class PreferencePredicateSpec extends ControllerBaseSpec with BeforeAndAfterAll {

  override def beforeAll(): Unit = mockConfig.features.preferenceJourneyEnabled(true)

  "The preference predicate" when {

    "the preference journey feature switch is on" when {

      "the agent has no preference or verified email in session" should {

        "allow the request to pass through the predicate" in {
          await(mockPreferencePredicate.refine(agent)) shouldBe Right(agent)
        }
      }

      "the agent has a preference of 'no' in session" should {

        "redirect the request to the SelectClientVrn controller" in {
          val agentWithPref = Agent(arn)(request.withSession(SessionKeys.preference -> "no"))
          await(mockPreferencePredicate.refine(agentWithPref)) shouldBe
            Left(Redirect(controllers.agent.routes.SelectClientVrnController.show("/customer-details")))
        }
      }

      "the agent has a preference of 'yes' and no verified email address in session" should {

        "allow the request to pass through the predicate" in {
          val agentWithPref = Agent(arn)(request.withSession(SessionKeys.preference -> "yes"))
          await(mockPreferencePredicate.refine(agentWithPref)) shouldBe Right(agentWithPref)
        }
      }

      "the agent has a preference of 'yes' and a verified email address in session" should {

        "redirect the request to the SelectClientVrn controller" in {
          val agentWithPref = Agent(arn)(request.withSession(
            SessionKeys.preference -> "yes",
            SessionKeys.verifiedAgentEmail -> "scala@gmail.com"
          ))
          await(mockPreferencePredicate.refine(agentWithPref)) shouldBe
            Left(Redirect(controllers.agent.routes.SelectClientVrnController.show("/customer-details")))
        }
      }
    }

    "the preference journey feature switch is off" should {

      lazy val result = {
        mockConfig.features.preferenceJourneyEnabled(false)
        await(mockPreferencePredicate.refine(agent))
      }

      "redirect the request to the SelectClientVrn controller" in {
        result shouldBe Left(Redirect(controllers.agent.routes.SelectClientVrnController.show("/homepage"))
          .addingToSession(SessionKeys.preference -> "no")(agent))
      }

      "add the 'no' preference to session to deny further attempts to bypass the predicate" in {
        result.left.get.session(agent).get(SessionKeys.preference) shouldBe Some("no")
      }
    }
  }
}
