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

package views.templates.inputs

import forms.ClientVrnForm
import play.api.data.{Field, FormError}
import play.twirl.api.Html
import views.templates.TemplateBaseSpec
import views.html.templates.inputs.Text

class TextTemplateSpec extends TemplateBaseSpec {

  val text: Text = inject[Text]

  "Rendering the text input" when {

    val fieldName = "fieldName"
    val labelText = "labelText"
    val additionalContent = Html("<p>Content</p>")
    val errorMessage = "error message"

    "the field is not populated" should {

      val field: Field = Field(ClientVrnForm.form, fieldName, Seq(), None, Seq(), None)

      val expectedMarkup = Html(
        s"""
           |<div class="form-group">
           |  <fieldset aria-describedby="form-hint">
           |    <div class="form-field">
           |      <h1 id="page-heading"><label for=$fieldName class="heading-large">labelText</label></h1>
           |      <input type="text" class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value=""/>
           |    </div>
           |  </fieldset>
           |</div>
        """.stripMargin
      )

      val markup = text(field, Some(labelText))

      "generate the correct markup" in {
        formatHtml(markup) shouldBe formatHtml(expectedMarkup)
      }
    }

    "the field is populated with valid data" should {

      val value = "text value"
      val field: Field = Field(ClientVrnForm.form, fieldName, Seq(), None, Seq(), Some(value))

      val expectedMarkup = Html(
        s"""
           |<div class="form-group">
           |  <fieldset aria-describedby="form-hint">
           |    <div class="form-field">
           |      <h1 id="page-heading"><label for=$fieldName class="heading-large">labelText</label></h1>
           |      <p>Content</p>
           |      <input type="text" class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value="$value"/>
           |    </div>
           |  </fieldset>
           |</div>
        """.stripMargin
      )

      val markup = text(field, Some(labelText), Some(additionalContent))

      "generate the correct markup" in {
        formatHtml(markup) shouldBe formatHtml(expectedMarkup)
      }
    }

    "the field is populated with invalid data" should {

      val field: Field = Field(ClientVrnForm.form, fieldName, Seq(), None, Seq(FormError(fieldName, errorMessage)), Some(""))

      val expectedMarkup = Html(
        s"""
           |<div class="form-group">
           |  <fieldset aria-describedby="form-hint form-error">
           |    <div class="form-field--error panel-border-narrow">
           |      <h1 id="page-heading"><label for=$fieldName class="heading-large">labelText</label></h1>
           |      <p>Content</p>
           |      <span id="form-error" class="error-message">
           |        <span class="visuallyhidden">Error:</span>
           |        $errorMessage
           |      </span>
           |      <input type="text" class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value=""/>
           |    </div>
           |  </fieldset>
           |</div>
        """.stripMargin
      )

      val markup = text(field, Some(labelText), Some(additionalContent))

      "generate the correct markup" in {
        formatHtml(markup) shouldBe formatHtml(expectedMarkup)
      }
    }
  }
}
