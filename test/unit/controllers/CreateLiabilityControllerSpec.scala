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

package controllers

import models.forms.LiabilityForm
import models.request.AuthenticatedRequest
import models.{Permission, _}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.test.Helpers._
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._
import views.html.create_liability

import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global

class CreateLiabilityControllerSpec extends ControllerBaseSpec {

  private val casesService = mock[CasesService]

  private def controller(permission: Set[Permission]) = new CreateLiabilityController(
    new RequestActionsWithPermissions(playBodyParsers, permission),
    casesService,
    mcc,
    realAppConfig
  )

  "GET" should {
    "redirect to unauthorised if not permitted" in {
      val request = newFakeGETRequestWithCSRF(app)
      val result  = await(controller(Set.empty).get()(request))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" in {
      val request = newFakeGETRequestWithCSRF(app)
      val result  = await(controller(Set(Permission.CREATE_CASES)).get()(request))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      contentAsString(result) shouldBe create_liability(LiabilityForm.newLiabilityForm)(
        AuthenticatedRequest(Operator("0", Some("name")), request),
        messages,
        realAppConfig
      ).toString()
    }
  }

  "POST" should {
    "redirect to unauthorised if not permitted" in {
      val request = newFakePOSTRequestWithCSRF(app)
      val result  = await(controller(Set.empty).post()(request))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "render view with errors" when {
      "form is invalid" in {
        val request   = newFakePOSTRequestWithCSRF(app)
        val result    = await(controller(Set(Permission.CREATE_CASES)).post()(request))
        lazy val form = LiabilityForm.newLiabilityForm.bindFromRequest()(request)

        status(result)      shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result)     shouldBe Some("utf-8")
        contentAsString(result) shouldBe create_liability(form)(
          AuthenticatedRequest(Operator("0", Some("name")), request),
          messages,
          realAppConfig
        ).toString()
      }
    }

    "redirect on success" in {
      given(casesService.createCase(any[LiabilityOrder], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(aCase(withReference("reference"))))
      val request = newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody(
        "item-name"        -> "item name",
        "trader-name"      -> "Trader",
        "liability-status" -> "LIVE"
      )

      val result = await(controller(Set(Permission.CREATE_CASES)).post()(request))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.CaseController.get("reference").url)
    }
  }

}
