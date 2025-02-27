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

package connector

import com.kenshoo.play.metrics.Metrics
import config.AppConfig
import javax.inject.{Inject, Singleton}
import metrics.HasMetrics
import models.PdfFile
import play.api.http.Status.OK
import play.api.libs.ws.WSClient
import play.twirl.api.Html
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.{failed, successful}

@Singleton
class PdfGeneratorServiceConnector @Inject() (
  configuration: AppConfig,
  ws: WSClient,
  val metrics: Metrics
)(implicit ec: ExecutionContext)
    extends HasMetrics {

  private lazy val url = s"${configuration.pdfGeneratorUrl}/pdf-generator-service/generate"

  def generatePdf(html: Html): Future[PdfFile] =
    withMetricsTimerAsync("generate-pdf") { _ =>
      ws.url(url).post(Map("html" -> Seq(html.toString))) flatMap { response =>
        response.status match {
          case OK => successful(PdfFile(content = response.bodyAsBytes.toArray))
          case _  => failed(new RuntimeException(s"Error calling pdf-generator-service - ${response.body}"))
        }
      }
    }

}
