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

package models.viewmodels

import models._

case class CaseHeaderViewModel(
  caseType: ApplicationType,
  businessName: Option[String],
  goodsName: String,
  referenceNumber: String,
  caseSource: Option[String],
  contact: Contact,
  caseStatus: CaseStatusViewModel,
  isMigrated : Boolean
)

object CaseHeaderViewModel {
  def fromCase(c: Case): CaseHeaderViewModel = {
    CaseHeaderViewModel(
      c.application.`type`,
      c.application.businessName,
      c.application.goodsName,
      c.reference,
      c.application.caseSource,
      c.application.contact,
      CaseStatusViewModel.fromCase(c),
      c.dateOfExtract.isDefined
    )
  }
}
