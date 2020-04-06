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

package models.viewmodels

import models.CaseStatus.CaseStatus
import models.{CancelReason, Case, CaseStatus, Operator}

case class CaseHeaderViewModel(
                                caseType: String,
                                businessName: String,
                                goodsName: String,
                                referenceNumber: String,
                                caseStatus: String,
                                isLive: Boolean
                              )

object CaseHeaderViewModel {

    def fromCase(c: Case): CaseHeaderViewModel = {
      val status: String = {
        c.status match {
          case CaseStatus.CANCELLED => s"CANCELLED${c.decision.flatMap(_.cancellation).map(c => CancelReason.code(c.reason)).map(s => s" - $s").getOrElse("")}"
          case CaseStatus.COMPLETED if c.hasExpiredRuling => "EXPIRED"
          case s: CaseStatus => s.toString
        }
      }

      CaseHeaderViewModel(
        "Liability",
        c.application.businessName,
        c.application.goodsName,
        c.reference,
        status,
        c.application.isLiveLiabilityOrder)
    }
}
