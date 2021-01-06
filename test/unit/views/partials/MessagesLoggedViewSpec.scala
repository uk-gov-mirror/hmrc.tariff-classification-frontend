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

package views.partials

import java.time.{Instant, ZoneOffset, ZonedDateTime}

import models.forms.MessageForm
import models.viewmodels.MessagesTabViewModel
import models.{CaseStatus, _}
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials

class MessagesLoggedViewSpec extends ViewSpec {

  private val date   = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant

  private val exampleMessages = List(Message("name", Instant.now(), "message"),
    Message("name2", Instant.now(), "message2"))

  private val messagesTab: MessagesTabViewModel = MessagesTabViewModel("reference", exampleMessages)

  "Messages Details" should {

    val requestWithAddNotePermission = requestWithPermissions(Permission.ADD_NOTE)

    "Render event without operator name" in {
      // Given
      val c = aCorrespondenceCase()

      val messagesTab = MessagesTabViewModel.fromCase(c)

      // When
      val doc = view(partials.messages_logged(messagesTab, MessageForm.form))

      // Then
      doc shouldNot  containElementWithID("activity-events-row-0-operator")
    }

    "Render 'Add Message' when user has permission" in {
      // Given
      val c = aCorrespondenceCase()

      val messagesTab = MessagesTabViewModel.fromCase(c)

      // When

      val doc = view(partials.messages_logged(messagesTab, MessageForm.form)
        (requestWithAddNotePermission, messages, appConfig)
      )

      // Then
      doc should containElementWithID("add-note-submit")
    }


    "Render 'Message'" in {
      // Given
      val c = aCorrespondenceCase()

      val messagesTab = MessagesTabViewModel.fromCase(c)

      // When

      val doc = view(partials.messages_logged(messagesTab, MessageForm.form)
          (requestWithAddNotePermission, messages, appConfig)
      )

      // Then
      doc                                                  should containElementWithID("messages-events-row-0-name")
      doc.getElementById("messages-events-row-0-name") should containText("name")
      doc                                                  should containElementWithID("messages-events-row-0-message")
      doc.getElementById("messages-events-row-0-message")  should containText("comment")
      doc                                                  should containElementWithID("messages-events-row-0-date")
      doc.getElementById("messages-events-row-0-date")     should containText("01 Jan 2019")
    }

  }
}