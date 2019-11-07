package views.agent.partials

import assets.messages.partials.{VatReturnsPartialMessages => Messages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec

class VatReturnsPartialSpec extends ViewBaseSpec {
  "VatReturnsPartial" should{
    lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
    lazy val view = views.html.agent.partials.vatReturnsPartial()(messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"have the correct card heading" in {
      elementText(".heading-medium") shouldBe Messages.heading
    }

    "display the correct line 1" in {
      elementText("#card-info") shouldBe Messages.paragraphOne
    }

    "display the correct line 2" in {
      elementText("#card-link") shouldBe Messages.submittedReturns
    }

    "display the correct line 2 with the correct link" in {
      element("#card-link").attr("href") shouldBe "/submitted-returns"
    }
  }

}
