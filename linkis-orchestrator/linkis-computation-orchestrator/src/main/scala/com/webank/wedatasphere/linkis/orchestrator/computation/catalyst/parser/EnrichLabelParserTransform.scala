/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.parser

import com.webank.wedatasphere.linkis.orchestrator.code.plans.ast.CodeJob
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.parser.label.{CacheParserLabelFiller, ParserLabelFiller}
import com.webank.wedatasphere.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ParserTransform
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, Job}

class EnrichLabelParserTransform extends ParserTransform {

  var parserLabelFillers: Array[ParserLabelFiller] = _

  initParserLabelFillers

  override def apply(in: Job, context: ASTContext): Job = {
    in match {
      case codeJob: CodeJob =>
        enrichLabels(codeJob, context)
        codeJob
      case _ => throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.PARSER_FOR_NOT_SUPPORT_ERROR_CODE,
        "EnrichLabelParserTransform Cannot convert job " + in)
    }
  }

  def enrichLabels(codeJob: CodeJob, context: ASTContext) = {
    parserLabelFillers.foreach{ filler =>
      filler.parseToLabel(codeJob, context) match {
        case Some(label) => context.getLabels.add(label)
        case None =>
      }
    }
  }

  def initParserLabelFillers = {
    parserLabelFillers = Array(new CacheParserLabelFiller)
  }

  override def getName: String = "EnrichLabelParserTransform"

}
