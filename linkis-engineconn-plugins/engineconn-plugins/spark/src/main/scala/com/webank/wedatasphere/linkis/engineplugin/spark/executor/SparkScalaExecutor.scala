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
 */

package com.webank.wedatasphere.linkis.engineplugin.spark.executor

import java.io.{BufferedReader, File}
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationExecutorManager
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineconn.computation.executor.rs.RsOutputStream
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineplugin.spark.common.{Kind, SparkScala}
import com.webank.wedatasphere.linkis.engineplugin.spark.config.SparkConfiguration
import com.webank.wedatasphere.linkis.engineplugin.spark.entity.SparkEngineSession
import com.webank.wedatasphere.linkis.engineplugin.spark.exception.{ApplicationAlreadyStoppedException, ExecuteError, SparkSessionNullException}
import com.webank.wedatasphere.linkis.engineplugin.spark.utils.EngineUtils
import com.webank.wedatasphere.linkis.governance.common.paser.ScalaCodeParser
import com.webank.wedatasphere.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, IncompleteExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetWriter
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.spark.repl.SparkILoop
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.util.SparkUtils
import org.apache.spark.{SparkConf, SparkContext}

import _root_.scala.tools.nsc.GenericRunnerSettings
import scala.tools.nsc.interpreter.{IMain, JPrintWriter, NamedParam, Results, SimpleReader, StdReplTags, isReplPower, replProps}


class SparkScalaExecutor(sparkEngineSession: SparkEngineSession, id: Long) extends SparkEngineConnExecutor(sparkEngineSession.sparkContext, id) {

  private val sparkContext: SparkContext = sparkEngineSession.sparkContext

  private val _sqlContext: SQLContext = sparkEngineSession.sqlContext

  private val sparkSession: SparkSession = sparkEngineSession.sparkSession

  private val sparkConf: SparkConf = sparkContext.getConf

  private var sparkILoop: SparkILoop = _

  private var bindFlag: Boolean = false

  private val engineExecutionContextFactory: EngineExecutionContextFactory = new EngineExecutionContextFactory

  private val lineOutputStream = new RsOutputStream

  private val jOut = new JPrintWriter(lineOutputStream, true)

  private val jobGroup = new java.lang.StringBuilder

  private var executeCount = 0

  var sparkILoopInited = false

  private val outputDir = sparkEngineSession.outputDir

  protected implicit val executor = Utils.newCachedExecutionContext(5, "Spark-Scala-REPL-Thread-", true)

  override def init(): Unit = {

    System.setProperty("scala.repl.name.line", ("$line" + this.hashCode).replace('-', '0'))

    setCodeParser(new ScalaCodeParser)

    if (sparkILoop == null) {
      synchronized {
        if (sparkILoop == null) createSparkILoop
      }
    }
    if (sparkILoop != null) {
      if (!sparkILoopInited) {
        sparkILoop synchronized {
          if (!sparkILoopInited) {
            Utils.tryCatch{
              initSparkILoop
            }{
              t =>
                logger.error("init failed: ", t)
                null
            }
            //TODO When an exception is thrown, is there a shutdown here? I need to think about it again.（当抛出异常时，此处是否需要shutdown，需要再思考一下）
            sparkILoopInited = true
          }
        }
      }
    }else{
      throw new SparkSessionNullException(40006,"sparkILoop is null")
    }
    Utils.waitUntil(() => sparkILoopInited && sparkILoop.intp != null, SparkConfiguration.SPARK_LOOP_INIT_TIME.getValue.toDuration)
    super.init()
  }


  override protected def getKind: Kind = SparkScala()

  override protected def runCode(executor: SparkEngineConnExecutor, code: String, engineExecutionContext: EngineExecutionContext, jobGroup: String): ExecuteResponse = {
    this.jobGroup.append(jobGroup)
    if (null != sparkILoop.intp && null != sparkILoop.intp.classLoader) {
      Thread.currentThread().setContextClassLoader(sparkILoop.intp.classLoader)
    }
    if (engineExecutionContext != this.engineExecutionContextFactory.getEngineExecutionContext) {
      lineOutputStream.reset(engineExecutionContext)
    }

    lazyLoadILoop
    lineOutputStream.ready()
    if(sparkILoopInited) {
      this.engineExecutionContextFactory.setEngineExecutionContext(engineExecutionContext)
    }
    var res: ExecuteResponse = null

    Utils.tryCatch{
      res = executeLine(code,engineExecutionContext)
    }{
      case e: Exception =>
        sparkContext.clearJobGroup()
        error("Interpreter exception", e)
        // _state = Idle()
        return ErrorExecuteResponse("Interpreter exception",e)
    }
    res match {
      case SuccessExecuteResponse() =>
      case IncompleteExecuteResponse(_) =>
      case _ =>
        sparkContext.clearJobGroup()
        return res
    }

    res
  }
  def executeLine(code: String, engineExecutionContext: EngineExecutionContext): ExecuteResponse = synchronized {
    if(sparkContext.isStopped) {
      error("Spark application has already stopped, please restart it.")
      throw new ApplicationAlreadyStoppedException(40004,"Spark application has already stopped, please restart it.")
    }
    executeCount += 1
    val originalOut = System.out
    val result = scala.Console.withOut(lineOutputStream) {
      Utils.tryCatch(sparkILoop.interpret(code)){ t =>
        error("task error info:", t)
        val msg = ExceptionUtils.getRootCauseMessage(t)
        if (msg.contains("OutOfMemoryError")) {
          error("engine oom now to set status to shutdown")
          ExecutorManager.getInstance.getReportExecutor.tryShutdown()
        }
        engineExecutionContext.appendStdout("task error info: " + msg)
        Results.Error
      } match {
        case Results.Success =>
          lineOutputStream.flush()
          engineExecutionContext.appendStdout("scala> " + code)
          val outStr = lineOutputStream.toString()
          if(outStr.length >0) {
            val output = Utils.tryQuietly(ResultSetWriter.getRecordByRes(outStr, SparkConfiguration.SPARK_CONSOLE_OUTPUT_NUM.getValue))
            val res = if (output != null) output.map(x => x.toString).toList.mkString("\n") else ""
            if (res.length > 0) {
              engineExecutionContext.appendStdout(res)
            }
          }
          SuccessExecuteResponse()
        case Results.Incomplete =>
          //error("incomplete code.")
          IncompleteExecuteResponse(null)
        case Results.Error =>
          lineOutputStream.flush()
          val output = lineOutputStream.toString
          IOUtils.closeQuietly(lineOutputStream)
          var errorMsg: String = null
            if (StringUtils.isNotBlank(output)) {
            errorMsg = Utils.tryCatch(EngineUtils.getResultStrByDolphinTextContent(output))(t => t.getMessage)
            error("Execute code error for "+  errorMsg)
          } else {
            error("No error message is captured, please see the detailed log")
          }
          ErrorExecuteResponse(errorMsg, ExecuteError(40005, "execute sparkScala failed!"))
      }
    }
    // reset the java stdout
    System.setOut(originalOut)
    result
  }



  private def createSparkILoop = {
    info("outputDir====> " + outputDir)
    sparkILoop = Utils.tryCatch{
      new SparkILoop(None, jOut)
    }{
      t => logger.error("create ILoop failed", t)
        null
    }
  }

  private def lazyLoadILoop = {    //lazy loaded.
    if(!bindFlag) {
      bindSparkSession
    }

  }

  private def initSparkILoop = {
    val settings = new GenericRunnerSettings(error(_))
    val sparkJars = sparkConf.getOption("spark.jars")
    val jars = if (sparkConf.get("spark.master").contains("yarn")) {
      val yarnJars = sparkConf.getOption("spark.yarn.dist.jars")
      SparkUtils.unionFileLists(sparkJars, yarnJars).toSeq
    } else {
      sparkJars.map(_.split(",")).map(_.filter(_.nonEmpty)).toSeq.flatten
    }
    val classpathJars = System.getProperty("java.class.path").split(":").filter(_.endsWith(".jar"))
    //.filterNot(f=> f.contains("spark-") || f.contains("datanucleus"))
    val classpath = jars.mkString(File.pathSeparator) + File.pathSeparator +
      classpathJars.mkString(File.pathSeparator)
    debug("Spark shell add jars: " + classpath)
    settings.processArguments(List("-Yrepl-class-based",
      "-Yrepl-outdir", s"${outputDir.getAbsolutePath}", "-classpath", classpath), true)
    settings.usejavacp.value = true
    settings.embeddedDefaults(Thread.currentThread().getContextClassLoader())
    sparkILoop.settings = settings
    sparkILoop.createInterpreter()

    val in0 = getField(sparkILoop, "scala$tools$nsc$interpreter$ILoop$$in0").asInstanceOf[Option[BufferedReader]]
    val reader = in0.fold(sparkILoop.chooseReader(settings))(r => SimpleReader(r,
      jOut, interactive = true))

    sparkILoop.in = reader
    sparkILoop.initializeSynchronous()
    SparkScalaExecutor.loopPostInit(sparkILoop)
  }

  protected def getField(obj: Object, name: String): Object = {
    val field = obj.getClass.getField(name)
    field.setAccessible(true)
    field.get(obj)
  }

  def bindSparkSession = {
    require(sparkContext != null)
    require(sparkSession != null)
    require(_sqlContext != null)
    //Wait up to 10 seconds（最多等待10秒）
    val startTime = System.currentTimeMillis()
    Utils.waitUntil(() => sparkILoop.intp != null && sparkILoop.intp.isInitializeComplete, SparkConfiguration.SPARK_LANGUAGE_REPL_INIT_TIME.getValue.toDuration)
    warn(s"Start to init sparkILoop cost ${System.currentTimeMillis() - startTime}.")
    sparkILoop.beSilentDuring {
      sparkILoop.command(":silent")
      sparkILoop.bind("sc", "org.apache.spark.SparkContext", sparkContext, List("""@transient"""))
      sparkILoop.bind("spark", "org.apache.spark.sql.SparkSession", sparkSession, List("""@transient"""))
      sparkILoop.bind("sqlContext", "org.apache.spark.sql.SQLContext", _sqlContext, List("""@transient"""))
      sparkILoop.bind("engineExecutionContextFactory", "com.webank.wedatasphere.linkis.engineplugin.spark.executor.EngineExecutionContextFactory", engineExecutionContextFactory)
      sparkILoop.bind("jobGroup", "java.lang.StringBuilder", jobGroup)

      sparkILoop.interpret("import org.apache.spark.SparkContext")
      sparkILoop.interpret("import org.apache.spark.SparkContext._")
      sparkILoop.interpret("import org.apache.spark.sql.SparkSession")
      sparkILoop.interpret("import org.apache.spark.sql.SQLContext")
      sparkILoop.interpret("import org.apache.spark.sql.DataFrame")
      sparkILoop.interpret("import com.webank.wedatasphere.linkis.engineplugin.spark.executor.SQLSession.showDF")
      sparkILoop.interpret("import com.webank.wedatasphere.linkis.engineplugin.spark.executor.SQLSession.showHTML")
      sparkILoop.interpret("import sqlContext.sql")
      sparkILoop.interpret("import sqlContext._")
      sparkILoop.interpret("import spark.implicits._")
      sparkILoop.interpret("import spark.sql")
      sparkILoop.interpret("import org.apache.spark.sql.functions._")
      sparkILoop.interpret("import com.webank.wedatasphere.linkis.engineplugin.spark.executor.EngineExecutionContextFactory")
      sparkILoop.interpret("def showAlias(df: DataFrame, alias:String): Unit = showDF(sparkContext, jobGroup.toString, df, alias,10000, engineExecutionContextFactory.getEngineExecutionContext)")
      sparkILoop.interpret("def show(df: DataFrame): Unit = showDF(sparkContext, jobGroup.toString, df,\"\",10000, engineExecutionContextFactory.getEngineExecutionContext)")
      sparkILoop.interpret("def showHtml(content: Any): Unit = showHTML(sparkContext, jobGroup.toString, content, engineExecutionContextFactory.getEngineExecutionContext)")
      sparkILoop.interpret("import org.apache.spark.sql.execution.datasources.csv._")
      sparkILoop.interpret("import org.apache.spark.sql.UDFRegistration")
      sparkILoop.interpret("val udf = UDF")
      sparkILoop.interpret("implicit def toUDFMethod(udf: UDF.type): UDFRegistration = sqlContext.udf")
      sparkILoop.interpret("implicit val sparkSession = spark")
      bindFlag = true
      warn(s"Finished to init sparkILoop cost ${System.currentTimeMillis() - startTime}.")
    }
  }

  def getOption(key: String): Option[String] = {
    val value = SparkConfiguration.SPARK_REPL_CLASSDIR.getValue
    Some(value)
  }

  override protected def getExecutorIdPreFix: String = "SparkScalaExecutor_"
}

class EngineExecutionContextFactory {
  private var engineExecutionContext: EngineExecutionContext = _

  def setEngineExecutionContext(engineExecutionContext: EngineExecutionContext): Unit = this.engineExecutionContext = engineExecutionContext

  def getEngineExecutionContext = this.engineExecutionContext
}

object SparkScalaExecutor {

  private def loopPostInit(sparkILoop: SparkILoop): Unit = {
    import StdReplTags._
    import scala.reflect.{classTag, io}

    val intp = sparkILoop.intp
    val power = sparkILoop.power
    val in = sparkILoop.in

    def loopPostInit() {
      // Bind intp somewhere out of the regular namespace where
      // we can get at it in generated code.
      intp.quietBind(NamedParam[IMain]("$intp", intp)(tagOfIMain, classTag[IMain]))
      // Auto-run code via some setting.
      (replProps.replAutorunCode.option
        flatMap (f => io.File(f).safeSlurp())
        foreach (intp quietRun _)
        )
      // classloader and power mode setup
      intp.setContextClassLoader()
      if (isReplPower) {
        replProps.power setValue true
        unleashAndSetPhase()
        asyncMessage(power.banner)
      }
      // SI-7418 Now, and only now, can we enable TAB completion.
      in.postInit()
    }

    def unleashAndSetPhase() = if (isReplPower) {
      power.unleash()
      intp beSilentDuring phaseCommand("typer") // Set the phase to "typer"
    }

    def phaseCommand(name: String): Results.Result = {
      callMethod(
        sparkILoop,
        "scala$tools$nsc$interpreter$ILoop$$phaseCommand",
        Array(classOf[String]),
        Array(name)).asInstanceOf[Results.Result]
    }

    def asyncMessage(msg: String): Unit = {
      callMethod(
        sparkILoop, "asyncMessage", Array(classOf[String]), Array(msg))
    }

    loopPostInit()
  }

  def callMethod(obj: Object, name: String,
                 parameterTypes: Array[Class[_]],
                 parameters: Array[Object]): Object = {
    val method = obj.getClass.getMethod(name, parameterTypes: _ *)
    method.setAccessible(true)
    method.invoke(obj, parameters: _ *)
  }

}