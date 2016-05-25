/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox

import play.api.hal.HalLink
import play.api.libs.json.Json._
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, Links}
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.ExecutionContext.Implicits.global

object SummaryController extends BaseController with Links {

  override lazy val context: String = AppContext.apiGatewayContext

  def handler(sourceType: SourceType, summaryTypeName: String): SummaryHandler[_] = {
    import SourceTypes._
    import SummaryTypes._
    val summaryType = sourceType.summaryTypes.find(_.name == summaryTypeName)
    (sourceType, summaryType) match {
      case (SelfEmployments, Some(SelfEmploymentIncomes)) => IncomesSummaryHandler
      case (SelfEmployments, Some(Expenses)) => ExpensesSummaryHandler
      case (SelfEmployments, Some(BalancingCharges)) => BalancingChargesSummaryHandler
      case (SelfEmployments, Some(GoodsAndServicesOwnUse)) => GoodsAndServiceOwnUseSummaryHandler
      case (FurnishedHolidayLettings, Some(PrivateUseAdjustment)) => PrivateUseAdjustmentSummaryHandler
      case (FurnishedHolidayLettings, Some(FurnishedHolidayLettingsIncome)) => FurnishedHolidayLettingsIncomeSummaryHandler
      case (UKProperty, Some(UKPropertyIncomes)) => UKPropertyIncomeSummaryHandler
      case _ => throw new IllegalArgumentException(s"""Unsupported combination of sourceType "${sourceType.name}" and "$summaryTypeName""")
    }
  }

  def create(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) = Action.async(parse.json) { implicit request =>
    handler(sourceType, summaryTypeName).create(request.body) map {
      case Left(errorResult) =>
        errorResult match {
          case ErrorResult(Some(message), _) => BadRequest(message)
          case ErrorResult(_, Some(errors)) => BadRequest(failedValidationJson(errors))
          case _ => BadRequest
        }
      case Right(id) =>
        Created(halResource(obj(), Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, id)))))
    }
  }

  def read(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = Action.async { implicit request =>
    handler(sourceType, summaryTypeName).findById(summaryId) map {
      case Some(summary) =>
        Ok(halResource(toJson(summary), Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)))))
      case None =>
        NotFound
    }
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = Action.async(parse.json) { implicit request =>
    handler(sourceType, summaryTypeName).update(summaryId, request.body) map {
      case Left(errorResult) =>
        errorResult match {
          case ErrorResult(Some(message), _) => BadRequest(message)
          case ErrorResult(_, Some(errors)) => BadRequest(failedValidationJson(errors))
          case _ => BadRequest
        }
      case Right(id) =>
        Ok(halResource(obj(), Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, id)))))
    }
  }


  def delete(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = Action.async { implicit request =>
    handler(sourceType, summaryTypeName).delete(summaryId) map {
      case true =>
        NoContent
      case false =>
        NotFound
    }
  }


  def list(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) = Action.async { implicit request =>
    val svc = handler(sourceType, summaryTypeName)
    svc.find map { summaryIds =>
      val json = toJson(summaryIds.map(id => halResource(obj(),
        Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, id))))))

      Ok(halResourceList(svc.listName, json, sourceTypeAndSummaryTypeHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName)))
    }
  }

}