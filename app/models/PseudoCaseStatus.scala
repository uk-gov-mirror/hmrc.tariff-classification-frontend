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

package models

object PseudoCaseStatus extends Enumeration {
  type PseudoCaseStatus = Value
  val DRAFT, NEW, OPEN, SUPPRESSED, REFERRED, REJECTED, CANCELLED, SUSPENDED, COMPLETED, REVOKED, ANNULLED, LIVE,
    EXPIRED, UNDER_APPEAL, UNDER_REVIEW = Value

  def format(status: PseudoCaseStatus.Value): String = status match {
    case UNDER_APPEAL => "UNDER APPEAL"
    case UNDER_REVIEW => "UNDER REVIEW"
    case _ => status.toString
  }
}
