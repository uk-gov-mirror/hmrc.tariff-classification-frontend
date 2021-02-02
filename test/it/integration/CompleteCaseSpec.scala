package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import models.{CaseStatus, Decision, Operator, Role}
import utils.{CasePayloads, Cases}
import utils.JsonFormatters._

class CompleteCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Complete with decision" should {

    val owner = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
    val completeDecision = Decision(
      bindingCommodityCode = "0300000000",
      justification        = "justification-content",
      goodsDescription     = "goods-description",
      methodSearch         = Some("method-to-search"),
      explanation          = Some("explanation")
    )
    val caseWithStatusOPEN = CasePayloads.jsonOf(
      Cases.btiCaseExample.copy(status = CaseStatus.OPEN, decision = Some(completeDecision), assignee = owner)
    )

    def shouldSucceed = {
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusOPEN)
          )
      )

      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/complete").get())

      // Then
      response.status shouldBe OK
      response.body   should include("Complete this case")
      response.body   should not include "disabled=disabled"
    }

    def shouldNotSucceed = {
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusOPEN)
          )
      )

      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/complete").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

    "return status 200 for manager" in {
      // Given
      givenAuthSuccess("manager")
      shouldSucceed
    }

    "return status 200 for read only" in {
      // Given
      givenAuthSuccess("read-only")
      shouldNotSucceed
    }

    "return status 200 for team member" in {
      givenAuthSuccess("team")
      shouldSucceed
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()
      shouldFail
    }

    "redirect for non case owner" in {
      // Given
      givenAuthSuccess("another team member")
      shouldFail
    }

    def shouldFail = {
      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/complete").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

}
