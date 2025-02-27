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

package views

import models.Case
import models.CaseStatus.OPEN

case class AssignedCases(name: String, openCases: Seq[Case], otherCases: Seq[Case])

object AssignedCases {

  def apply(cases: Seq[Case], assigneeId: Option[String]): Option[AssignedCases] =
    assigneeId.map(build(_, cases))

  private def build(opId: String, cases: Seq[Case]): AssignedCases = {
    val opCases                 = cases.filter(_.assignee.exists(_.id == opId))
    val name                    = opCases.headOption.flatMap(_.assignee.flatMap(_.name)).getOrElse("")
    val (openCases, otherCases) = opCases.partition(_.status == OPEN)
    AssignedCases(name, openCases, otherCases)
  }

}
