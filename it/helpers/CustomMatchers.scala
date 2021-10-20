/*
 * Copyright 2021 HM Revenue & Customs
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

import org.jsoup.Jsoup
import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.ws.WSResponse

trait CustomMatchers extends AnyWordSpecLike with Matchers with GivenWhenThen {

  def httpStatus(expectedValue: Int): HavePropertyMatcher[WSResponse, Int] =
    (response: WSResponse) => {
      Then(s"the response status should be '$expectedValue'")
      HavePropertyMatchResult(
        response.status == expectedValue,
        "httpStatus",
        expectedValue,
        response.status
      )
    }

  def pageTitle(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      Then(s"the page title should be '$expectedValue'")
      HavePropertyMatchResult(
        body.title == expectedValue,
        "pageTitle",
        expectedValue,
        body.title
      )
    }

  def elementText(cssSelector: String)(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      Then(s"the text of '$cssSelector' should be '$expectedValue'")

      HavePropertyMatchResult(
        body.select(cssSelector).text == expectedValue,
        cssSelector,
        expectedValue,
        body.select(cssSelector).text
      )
    }

  def elementWithLinkTo(cssSelector: String)(link: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      Then(s"the link of '$cssSelector' should be '$link'")

      HavePropertyMatchResult(
        body.select(cssSelector).attr("href") == link,
        cssSelector,
        link,
        body.select(cssSelector).attr("href")
      )
    }

  def redirectURI(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val redirectLocation: Option[String] = response.header("Location")
      Then(s"the redirect location should be '$expectedValue'")
      HavePropertyMatchResult(
        redirectLocation.contains(expectedValue),
        "redirectURI",
        expectedValue,
        redirectLocation.getOrElse("")
      )
    }

  def isElementVisible(cssSelector: String)(isVisible: Boolean): HavePropertyMatcher[WSResponse, Boolean] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      Then(s"it is $isVisible that '$cssSelector' should be on the page")

      HavePropertyMatchResult(
        !body.select(cssSelector).isEmpty == isVisible,
        cssSelector,
        isVisible,
        !body.select(cssSelector).isEmpty
      )
    }
}