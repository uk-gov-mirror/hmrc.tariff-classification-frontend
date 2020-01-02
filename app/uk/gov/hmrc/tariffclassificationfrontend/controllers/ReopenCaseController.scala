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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedCaseRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{ApplicationType, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ReopenCaseController @Inject()(verify: RequestActions,
                                     casesService: CasesService,
                                     val messagesApi: MessagesApi,
                                     implicit val appConfig: AppConfig) extends RenderCaseAction {

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  def confirmReopenCase(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.REOPEN_CASE)).async { implicit request: AuthenticatedCaseRequest[AnyContent] =>
      validateAndRedirect(_ => casesService.reopenCase(request.`case`, request.operator)
        .map(updatedCase => updatedCase.application.`type` match {
          case ApplicationType.BTI => routes.CaseController.applicantDetails(updatedCase.reference)
          case ApplicationType.LIABILITY_ORDER => routes.LiabilityController.liabilityDetails(updatedCase.reference)
        }
        )
      )
    }
}
