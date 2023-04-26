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

package connectors

import assets.PenaltiesConstants.penaltiesSummaryAsModel
import mocks.MockHttp
import play.api.test.Helpers._
import utils.TestUtil

class PenaltiesConnectorSpec extends TestUtil with MockHttp {

  val connector: PenaltiesConnector = new PenaltiesConnector(mockHttp, mockConfig)

  "Calling the penalties service" when {

      "return 200 and a PenaltiesSummary model" in {
        setupMockHttpGet(s"/vat/penalties/summary/123")(Right(penaltiesSummaryAsModel))
        await(connector.getPenaltiesDataForVRN("123")) shouldBe Right(penaltiesSummaryAsModel)
    }

  }
}
