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

package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.planner

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.code.plans.ast.{CodeJob, CodeStage}
import com.webank.wedatasphere.linkis.orchestrator.code.plans.logical.{CodeLogicalUnitTask, CodeLogicalUnitTaskDesc}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.PlannerTransform
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.{ASTContext, Job, Stage}
import com.webank.wedatasphere.linkis.orchestrator.plans.logical._
import com.webank.wedatasphere.linkis.orchestrator.plans.unit.CodeLogicalUnit

import scala.collection.mutable.ArrayBuffer

/**
 *
 *
 */
class TaskPlannerTransform extends PlannerTransform with Logging {

  def rebuildTreeNode(tmpTask: Task): Task = {
    tmpTask.getChildren.foreach(child => {
      val newParents = child.getParents.clone() :+ tmpTask
      child.withNewParents(newParents)
    })
    tmpTask
  }

  def buildCodeLogicTaskTree(codeLogicalUnit: CodeLogicalUnit = null, stage: Stage, startJobTask: Task = null): (Task, Task) = {
    val (stageTask, newStartJobTask) = buildStageTaskTree(StartStageTaskDesc(stage), startJobTask)
    val codeLogicalUnitTaskTmp = new CodeLogicalUnitTask(Array(), Array(stageTask))
    codeLogicalUnitTaskTmp.setTaskDesc(CodeLogicalUnitTaskDesc(stage.getJob))
    if(codeLogicalUnit != null) codeLogicalUnitTaskTmp.setCodeLogicalUnit(codeLogicalUnit)
    (rebuildTreeNode(codeLogicalUnitTaskTmp), newStartJobTask)
  }

  def buildStageTaskTree(taskDesc: StageTaskDesc, startJobTask: Task = null): (Task,Task) = {
    taskDesc match {
      case endStageTask: EndStageTaskDesc => {
        val (task, newStartJobTask) = buildCodeLogicTaskTree(taskDesc.stage.getJob match{
          case codeJob: CodeJob => codeJob.getCodeLogicalUnit
          case job: Job => {
            error(s"jobId:${job.getId}-----jobType:${job.getName}, job type mismatch, only support CodeJob")
            null
          }
        }, taskDesc.stage, startJobTask)
        val stageTaskTmp  = new StageTask(Array(), Array(task))
        stageTaskTmp.setTaskDesc(endStageTask)
        (rebuildTreeNode(stageTaskTmp), newStartJobTask)
      }
      case startStageTask: StartStageTaskDesc => {
        /** when the construction node arrives at stage-task-start
         *  check whether this stage has child nodes
         *  if true -> use the same way to build another stage tasks
         *  if false -> build or reuse job-task-start and points to the stage-task-start
         * */
        if(null == startStageTask.stage.getChildren || startStageTask.stage.getChildren.isEmpty){
          var newStartJobTask: Task = null
          val stageTaskTmp = new StageTask(Array(),
            if(startJobTask == null) {
              newStartJobTask = buildJobTaskTree(StartJobTaskDesc(startStageTask.stage.getJob))
              Array(newStartJobTask)
            }
            else {
              newStartJobTask = startJobTask
              Array(newStartJobTask)
            })
          stageTaskTmp.setTaskDesc(startStageTask)
          (rebuildTreeNode(stageTaskTmp), newStartJobTask)
        }else{
          val (stageTasks, newStartJobTask) = buildAllStageTaskTree(taskDesc.stage.getChildren, startJobTask)
          val stageTaskTmp = new StageTask(Array(), stageTasks)
          stageTaskTmp.setTaskDesc(taskDesc)
          (rebuildTreeNode(stageTaskTmp), newStartJobTask)
        }
      }
    }
  }

  def buildAllStageTaskTree(stages: Array[Stage], startJobTask: Task = null): (Array[Task], Task) = {
    val stageTasks = ArrayBuffer[Task]()
    var reusedStartJobTask: Task = startJobTask
    stages.foreach(stage => {
      val (stageTask, startJobTask) = buildStageTaskTree(EndStageTaskDesc(stage), reusedStartJobTask)
      reusedStartJobTask = startJobTask
      stageTasks += stageTask
    })
    (stageTasks.toArray, reusedStartJobTask)
  }

  def buildJobTaskTree(taskDesc: TaskDesc): Task = {
    taskDesc match {
      case startTask: StartJobTaskDesc => {
          /**
          * The end of recursion
          */
          val jobTask = new JobTask(Array(), Array())
          jobTask.setTaskDesc(startTask)
          jobTask
      }
      case endTask: EndJobTaskDesc => {
        val jobTaskTmp = new JobTask(Array(), buildAllStageTaskTree(endTask.job.getAllStages)._1)
        jobTaskTmp.setTaskDesc(endTask)
        rebuildTreeNode(jobTaskTmp)
      }
    }
  }

  override def apply(in: Job, context: ASTContext): Task = {
    in match {
      case job: CodeJob =>
        // TODO rebuild needed:  Notice( Stages maybe have dependency relation.)
        // TODO This class should be split into two kind of transforms.
        // TODO First, two PlannerTransforms are needed: one to transform Job to JobTaskEnd, one to transform Job to StageTaskEnd.
        // TODO Second, AnalyzeTransforms are needed: one for adding a computationTask by stage for no computation strategy,
        //  one to transform Job to JobTaskStart, one to transform Job to StageTaskStart.
        buildJobTaskTree(EndJobTaskDesc(job))
      case _ => error(s"unknown job type:${in.getClass} ")
        null
    }
  }

  override def getName: String = {
    val className = getClass.getName
    if (className endsWith "$") className.dropRight(1) else className
  }
}
