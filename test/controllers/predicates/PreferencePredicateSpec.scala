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

package controllers.predicates

import assets.BaseTestConstants._
import common.SessionKeys
import controllers.ControllerBaseSpec
import models.Agent
import org.scalatest.BeforeAndAfterAll
import play.api.mvc.Results.Redirect
import play.api.test.Helpers.{await, defaultAwaitTimeout}

class PreferencePredicateSpec extends ControllerBaseSpec with BeforeAndAfterAll {

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
            Left(Redirect("/customer-details"))
        }
      }

      "the agent has a preference of 'no' in session and no redirect url in session" should {

        "redirect the request to the change customer details page" in {

          val agentWithPref = Agent(arn)(request.withSession(SessionKeys.preference -> "no"))
          await(mockPreferencePredicate.refine(agentWithPref)) shouldBe
            Left(Redirect("/customer-details"))
        }
      }

      "the agent has a preference of 'no' in session with a redirect url in session" should {

        "redirect the request to the url held in session" in {

          val agentWithPref = Agent(arn)(request.withSession(
            SessionKeys.preference -> "no",
            SessionKeys.redirectUrl -> "/thisisntthechangepage"
          ))
          await(mockPreferencePredicate.refine(agentWithPref)) shouldBe
            Left(Redirect("/thisisntthechangepage"))
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
            SessionKeys.verifiedEmail -> "scala@gmail.com"
          ))
          await(mockPreferencePredicate.refine(agentWithPref)) shouldBe
            Left(Redirect("/customer-details"))
        }
      }

      "the agent has a preference of 'yes' and a verified email address in session and a redirect url in session" should {

        "redirect the request to the url held in session" in {

          val agentWithPref = Agent(arn)(request.withSession(
            SessionKeys.preference -> "yes",
            SessionKeys.verifiedEmail -> "scala@gmail.com",
            SessionKeys.redirectUrl -> "/immafiringmuhlaserbwaaaaaaaaaaah"
          ))
          await(mockPreferencePredicate.refine(agentWithPref)) shouldBe
            Left(Redirect("/immafiringmuhlaserbwaaaaaaaaaaah"))
        }
      }
    }

  }
}
