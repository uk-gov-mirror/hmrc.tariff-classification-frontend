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

package controllers.v2

import config.AppConfig
import controllers.RequestActions
import javax.inject.{Inject, Singleton}
import models.forms._
import models.request._
import models.viewmodels.atar._
import models.viewmodels.miscellaneous.DetailsViewModel
import models.viewmodels.{ActivityViewModel, CaseViewModel, MessagesTabViewModel, SampleStatusTabViewModel}
import models.{Case, EventType, NoPagination}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MiscellaneousController @Inject() (
  verify: RequestActions,
  eventsService: EventsService,
  queuesService: QueuesService,
  fileService: FileStoreService,
  mcc: MessagesControllerComponents,
  val miscellaneousView: views.html.v2.miscellaneous_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def displayMiscellaneous(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async(implicit request => renderView())

  def renderView(
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    messageForm: Form[MessageFormData]   = MessageForm.form,
    uploadForm: Form[String]             = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    val miscellaneousCase: Case         = request.`case`
    val miscellaneousViewModel          = CaseViewModel.fromCase(miscellaneousCase, request.operator)
    val caseDetailsTab                  = DetailsViewModel.fromCase(miscellaneousCase)
    val messagesTab                     = MessagesTabViewModel.fromCase(miscellaneousCase)
    val attachmentsTabViewModel         = getAttachmentTab(miscellaneousCase)
    val activityTabViewModel            = getActivityTab(miscellaneousCase)
    val storedAttachments               = fileService.getAttachments(miscellaneousCase)
    val miscellaneousSampleTabViewModel = getSampleTab(miscellaneousCase)

    for {
      attachmentsTab <- attachmentsTabViewModel
      activityTab    <- activityTabViewModel
      attachments    <- storedAttachments
      sampleTab      <- miscellaneousSampleTabViewModel

    } yield Ok(
      miscellaneousView(
        miscellaneousViewModel,
        caseDetailsTab,
        messagesTab,
        messageForm,
        sampleTab,
        attachmentsTab,
        uploadForm,
        activityTab,
        activityForm,
        attachments
      )
    )
  }

  private def getSampleTab(miscellaneousCase: Case)(implicit request: AuthenticatedRequest[_]) =
    eventsService.getFilteredEvents(miscellaneousCase.reference, NoPagination(), Some(EventType.sampleEvents)).map {
      sampleEvents => SampleStatusTabViewModel(miscellaneousCase.reference, miscellaneousCase.sample, sampleEvents)
    }

  private def getAttachmentTab(miscellaneousCase: Case)(implicit hc: HeaderCarrier): Future[AttachmentsTabViewModel] =
    fileService
      .getAttachments(miscellaneousCase)
      .map(attachments => AttachmentsTabViewModel.fromCase(miscellaneousCase, attachments))

  private def getActivityTab(
    miscellaneousCase: Case
  )(implicit request: AuthenticatedRequest[_]): Future[ActivityViewModel] =
    for {
      events <- eventsService
                 .getFilteredEvents(miscellaneousCase.reference, NoPagination(), Some(EventType.nonSampleEvents))
      queues <- queuesService.getAll
    } yield ActivityViewModel.fromCase(miscellaneousCase, events, queues)

}
