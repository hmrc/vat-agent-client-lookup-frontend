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

package config

import config.features.{Feature, Features}
import play.api.Configuration
import utils.TestUtil

class FeatureSpec extends TestUtil {
  private val features = new Features(app.injector.instanceOf[Configuration])
  val allFeatures: Seq[Feature] = Seq(
    features.useStaticDateFeature,
    features.emailVerificationEnabled
  )
  override def beforeEach(): Unit = {
    super.beforeEach()
    allFeatures.foreach(
      _(true)
    )
  }

  allFeatures.foreach {
    feature => {
      s"The ${feature.key} feature" should {
        "return its current state" in {
          feature() shouldEqual true
        }
        "switch to a new state" in {
          feature(false)
          feature() shouldEqual false
        }
      }
    }
  }
}
