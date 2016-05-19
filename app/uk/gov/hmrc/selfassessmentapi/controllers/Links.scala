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

package uk.gov.hmrc.selfassessmentapi.controllers

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain.{SummaryType, TaxYear}

trait Links {

  val context: String

  private def createLink(endpointUrl: String) = s"/$context$endpointUrl"

  def discoverTaxYearsHref(utr: SaUtr): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.TaxYearsDiscoveryController.discoverTaxYears(utr).url)

  def discoverTaxYearHref(utr: SaUtr, taxYear: TaxYear): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.TaxYearDiscoveryController.discoverTaxYear(utr, taxYear).url)

  def liabilityHref(utr: SaUtr, taxYear: TaxYear, liabilityId: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.LiabilityController.retrieveLiability(utr, taxYear, liabilityId).url)

  def liabilitiesHref(utr: SaUtr, taxYear: TaxYear): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.LiabilityController.find(utr, taxYear).url)

  def selfEmploymentHref(utr: SaUtr, taxYear: TaxYear, seId: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SelfEmploymentsController.findById(utr, taxYear, seId).url)

  def selfEmploymentsHref(utr: SaUtr, taxYear: TaxYear): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SelfEmploymentsController.find(utr, taxYear).url)

  def selfEmploymentSummaryTypeHref(utr: SaUtr, taxYear: TaxYear, seId: String, summaryType: SummaryType): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.NotImplementedSelfEmploymentSummariesController.find(utr, taxYear, seId, summaryType).url)

  def selfEmploymentSummaryTypeIdHref(utr: SaUtr, taxYear: TaxYear, seId: String, summaryType: SummaryType, id: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.NotImplementedSelfEmploymentSummariesController.findById(utr, taxYear, seId, summaryType, id).url)


}
