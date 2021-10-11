/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.scheduler.executer

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.scheduler.exception.SchedulerErrorException
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState._
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener


abstract class AbstractExecutor(id: Long) extends Executor with Logging {

  private var _state: ExecutorState = Starting
  private var lastActivityTime = System.currentTimeMillis
  private var executorListener: Option[ExecutorListener] = None

  def setExecutorListener(executorListener: ExecutorListener): Unit =
    this.executorListener = Some(executorListener)

  protected def callback(): Unit

  protected def isIdle = _state == Idle

  protected def isBusy = _state == Busy

  protected def whenBusy[A](f: => A) = whenState(Busy, f)

  protected def whenIdle[A](f: => A) = whenState(Idle, f)

  protected def whenState[A](state: ExecutorState, f: => A) = if (_state == state) f

  protected def ensureBusy[A](f: => A): A = {
    lastActivityTime = System.currentTimeMillis
    if (_state == Busy) synchronized {
      if (_state == Busy) return f
    }
    throw new SchedulerErrorException(20001, "%s is in state %s." format(toString, _state))
  }

  protected def ensureIdle[A](f: => A): A = ensureIdle(f, true)

  protected def ensureIdle[A](f: => A, transitionState: Boolean): A = {
    if (_state == Idle) synchronized {
      if (_state == Idle) {
        if (transitionState) transition(Busy)
        return Utils.tryFinally(f) {
          if (transitionState) transition(Idle)
          callback()
        }
      }
    }
    throw new SchedulerErrorException(20001, "%s is in state %s." format(toString, _state))
  }

  protected def ensureAvailable[A](f: => A): A = {
    if (ExecutorState.isAvailable(_state)) synchronized {
      if (ExecutorState.isAvailable(_state)) return Utils.tryFinally(f)(callback())
    }
    throw new SchedulerErrorException(20001, "%s is in state %s." format(toString, _state))
  }

  protected def whenAvailable[A](f: => A): A = {
    if (ExecutorState.isAvailable(_state)) return Utils.tryFinally(f)(callback())
    throw new SchedulerErrorException(20001, "%s is in state %s." format (toString, _state))
  }


  protected def transition(state: ExecutorState) = this synchronized {
    lastActivityTime = System.currentTimeMillis
    this._state match {
      case Error | Dead | Success =>
        warn(s"$toString attempt to change state ${this._state} => $state, ignore it.")
      case ShuttingDown =>
        state match {
          case Error | Dead | Success =>
            val oldState = _state
            this._state = state
            executorListener.foreach(_.onExecutorStateChanged(this, oldState, state))
          case _ => warn(s"$toString attempt to change a ShuttingDown session to $state, ignore it.")
        }
      case _ =>
        info(s"$toString change state ${_state} => $state.")
        val oldState = _state
        this._state = state
        executorListener.foreach(_.onExecutorStateChanged(this, oldState, state))
    }
  }

  override def getId: Long = id

  override def state: ExecutorState = _state

  override def getExecutorInfo: ExecutorInfo = ExecutorInfo(id, _state)

  def getLastActivityTime = lastActivityTime

  def setLastActivityTime(lastActivityTime:Long):Unit = this.lastActivityTime = lastActivityTime

}
