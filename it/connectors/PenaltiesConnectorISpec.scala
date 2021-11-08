
package connectors

import config.AppConfig
import helpers.IntegrationBaseSpec
import models.penalties.PenaltiesSummary
import play.api.libs.json.Json
import play.api.test.Helpers._
import stubs.PenaltiesStub
import uk.gov.hmrc.http.HeaderCarrier

class PenaltiesConnectorISpec extends IntegrationBaseSpec {

  private trait Test {
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val connector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

  "calling getPenaltiesDataForVRN" when {
    "when the feature switch is enabled" should {
      "return a successful response and PenaltySummary model from the penalties API" in new Test {
        appConfig.features.penaltiesServiceFeature(true)

        val responseBody = Json.parse(
          """
            |{
            |  "noOfPoints": 3,
            |  "noOfEstimatedPenalties": 2,
            |  "noOfCrystalisedPenalties": 1,
            |  "estimatedPenaltyAmount": 123.45,
            |  "crystalisedPenaltyAmountDue": 54.32,
            |  "hasAnyPenaltyData": true
            |}
            |""".stripMargin)
        PenaltiesStub.stubPenaltiesSummary(OK, responseBody, "123")
        val expectedContent: PenaltiesSummary = PenaltiesSummary(
          noOfPoints = 3,
          noOfEstimatedPenalties = 2,
          noOfCrystalisedPenalties = 1,
          estimatedPenaltyAmount = 123.45,
          crystalisedPenaltyAmountDue = 54.32,
          hasAnyPenaltyData = true
        )

        val result = await(connector.getPenaltiesDataForVRN("123"))
        result.get shouldBe Right(expectedContent)
      }

      "return an Empty PenaltiesSummary model when given an invalid vrn" in new Test {
        appConfig.features.penaltiesServiceFeature(true)
        val responseBody = Json.parse(
          """
            |{
            | "code": "foo",
            | "message": "bar"
            |}
            |""".stripMargin)
        PenaltiesStub.stubPenaltiesSummary(NOT_FOUND, responseBody, "123")
        val expectedContent: PenaltiesSummary = PenaltiesSummary.empty

        val result = await(connector.getPenaltiesDataForVRN("123"))
        result.get shouldBe Right(expectedContent)
      }
    }

    "when the feature switch is disabled" should {
      "return None" in new Test {
        appConfig.features.penaltiesServiceFeature(false)
        val responseBody = Json.parse(
          """
            |{
            |}
            |""".stripMargin)
        PenaltiesStub.stubPenaltiesSummary(OK, responseBody, "123")
        val result = await(connector.getPenaltiesDataForVRN("123"))
        result shouldBe None
      }
    }
  }
}