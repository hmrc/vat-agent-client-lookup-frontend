/*
 * Copyright 2020 HM Revenue & Customs
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

package views.templates.inputs

import forms.ClientVrnForm
import play.api.data.{Field, FormError}
import play.twirl.api.Html
import views.templates.TemplateBaseSpec

class TextTemplateSpec extends TemplateBaseSpec {

  "Rendering the text input" when {

    val fieldName = "fieldName"
    val labelText = "labelText"
    val additionalContent = Html("<p>Content</p>")
    val errorMessage = "error message"

    "the field is not populated" should {

      val field: Field = Field(ClientVrnForm.form, fieldName, Seq(), None, Seq(), None)

      val expectedMarkup = Html(
        s"""
           |
           |<div class="form-field">
           |  <h1>labelText</h1>
           |  <label for="$fieldName" class="form-label visuallyhidden">labelText</label>
           |  <input type="text" class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value=""/>
           |</div>
           |
        """.stripMargin
      )

      val markup = views.html.templates.inputs.text(field, labelText)

      "generate the correct markup" in {
        formatHtml(markup) shouldBe formatHtml(expectedMarkup)
      }
    }

    "the field is populated with valid data" should {

      val value = "text value"
      val field: Field = Field(ClientVrnForm.form, fieldName, Seq(), None, Seq(), Some(value))

      val expectedMarkup = Html(
        s"""
           |
           |<div class="form-field">
           |  <h1>labelText</h1>
           |  <p>Content</p>
           |  <label for="$fieldName" class="form-label visuallyhidden">labelText</label>
           |  <input type="text" class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value="$value"/>
           |</div>
           |
        """.stripMargin
      )

      val markup = views.html.templates.inputs.text(field, labelText, Some(additionalContent))

      "generate the correct markup" in {
        formatHtml(markup) shouldBe formatHtml(expectedMarkup)
      }
    }

    "the field is populated with invalid data" should {

      val field: Field = Field(ClientVrnForm.form, fieldName, Seq(), None, Seq(FormError(fieldName, errorMessage)), Some(""))

      val expectedMarkup = Html(
        s"""
           |
           |<div class="form-field--error">
           |  <h1>labelText</h1>
           |  <p>Content</p>
           |  <label for="$fieldName" class="form-label visuallyhidden">labelText</label>
           |  <span class="error-message" role="tooltip">
           |    $errorMessage
           |  </span>
           |  <input type="text" class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value=""/>
           |</div>
           |
        """.stripMargin
      )

      val markup = views.html.templates.inputs.text(field, labelText, Some(additionalContent))

      "generate the correct markup" in {
        formatHtml(markup) shouldBe formatHtml(expectedMarkup)
      }
    }
  }
}
