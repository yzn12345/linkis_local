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
package com.webank.wedatasphere.linkis.ujes.jdbc

import java.io.{InputStream, Reader}
import java.math.MathContext
import java.net.URL
import java.sql.{Blob, Clob, Connection, Date, NClob, Ref, ResultSet, RowId, SQLWarning, SQLXML, Statement, Time, Timestamp}
import java.util.Calendar
import java.{sql, util}

import com.webank.wedatasphere.linkis.ujes.client.request.ResultSetAction
import com.webank.wedatasphere.linkis.ujes.client.response.ResultSetResult
import org.apache.commons.lang.StringUtils




class UJESSQLResultSet(resultSetList: Array[String], ujesStatement: UJESSQLStatement, maxRows: Int, fetchSize: Int) extends ResultSet {

  private var currentRowCursor : Int = -1

  //All data of table, where each element represents a row
  private var resultSetRow : util.ArrayList[util.ArrayList[String]] = _

  private var resultSetResult: ResultSetResult = _

  private var resultSetMetaData: UJESSQLResultSetMetaData = new UJESSQLResultSetMetaData


  private var fetchSizeNum : Int = _

  private var currentRow : util.ArrayList[String] = _

  private var hasClosed : Boolean = false

  private var path : String = null

  private var metaData : util.List[util.Map[String, String]] = _

  private val statement : UJESSQLStatement = ujesStatement

  private val connection : UJESSQLConnection = ujesStatement.getConnection.asInstanceOf[UJESSQLConnection]

  private var valueWasNull : Boolean = false

  private var warningChain : SQLWarning = null

  init()

  private def getResultSetPath(resultSetList: Array[String]): String = {
    if (resultSetList.length > 0){
      resultSetList(resultSetList.length-1)
    } else {
      ""
    }
  }

  private def resultSetResultInit(): Unit = {
    if (path == null) path = getResultSetPath(resultSetList)
    val user = connection.getProps.getProperty("user")
    if (StringUtils.isNotBlank(path)){
      val resultAction = ResultSetAction.builder.setUser(user).setPath(path).build()
      resultSetResult = connection.ujesClient.resultSet(resultAction)
    }
  }

  private def metaDataInit(): Unit = {
    if ( null == resultSetResult ){
      return
    }
    metaData = resultSetResult.getMetadata.asInstanceOf[util.List[util.Map[String, String]]]
    for(cursor <- 1 to metaData.size()){
      val col = metaData.get(cursor - 1)
      resultSetMetaData.setColumnNameProperties(cursor, col.get("columnName"))
      resultSetMetaData.setDataTypeProperties(cursor, col.get("dataType"))
      resultSetMetaData.setCommentPropreties(cursor, col.get("comment"))
    }
  }

  private def resultSetInit(): Unit  = {
    if ( null == resultSetResult ){
      return
    }
    resultSetRow = resultSetResult.getFileContent.asInstanceOf[util.ArrayList[util.ArrayList[String]]]
  }

  private def init(): Unit = {
    resultSetResultInit()
    metaDataInit()
    resultSetInit()
  }
  private def updateCurrentRow(currentRowCursor : Int): Unit = {
    currentRow = currentRowCursor match {
      case cursor if cursor < 0 || cursor > resultSetRow.size()-1 => null
      case _ => resultSetRow.get(currentRowCursor)
    }
  }

  override def next(): Boolean = {
    if(metaData == null) init()
    currentRowCursor += 1
    if(null == resultSetRow || currentRowCursor > resultSetRow.size()-1) false
    else{
      updateCurrentRow(currentRowCursor)
      true
    }
  }

  def realClose(connection: Connection): Unit = {
    connection.close()
  }

  override def close(): Unit = {
    if(!isClosed){
      resultSetResult = null
      metaData = null
      resultSetMetaData = null
      resultSetRow = null
      currentRow = null
      hasClosed = true
    }
  }

  override def wasNull(): Boolean = {
    valueWasNull
  }

  private def evaluate(dataType: String,value: String): Any = {
    if(value == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR,"value is null")
    }else{
      dataType.toLowerCase match {
        case null => throw new UJESSQLException(UJESSQLErrorCode.METADATA_EMPTY)
        case "string" => value.toString
        case "short" => value.toShort
        case "int" => value.toInt
        case "long" => value.toLong
        case "float" => value.toFloat
        case "double" => value.toDouble
        case "boolean" => value.toBoolean
        case "byte" => value.toByte
        case "char" => value.toString.charAt(0)
        case "timestamp" => value.toString
        case _ => throw new UJESSQLException(UJESSQLErrorCode.PREPARESTATEMENT_TYPEERROR,
          s"Can't infer the SQL type to use for an instance of ${dataType}. Use getObject() with an explicit Types value to specify the type to use")
      }
    }
  }

  private def getColumnValue(columnIndex: Int): Any = {
    if(currentRow == null) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "No row found.")
    }else if(currentRow.size() <= 0){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "RowSet does not contain any columns!")
    }else if(columnIndex > currentRow.size()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, s" Invalid columnIndex: ${columnIndex}")
    }else{
      val dataType = resultSetMetaData.getColumnTypeName(columnIndex)
      val evaluateValue = evaluate(dataType,currentRow.get(columnIndex-1))
      valueWasNull = evaluateValue == null
      evaluateValue
    }

  }

  override def getString(columnIndex: Int): String = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[String]
    }
  }

  override def getBoolean(columnIndex: Int): Boolean = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[Boolean]
    }
  }

  override def getByte(columnIndex: Int): Byte = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[Byte]
    }
  }

  override def getShort(columnIndex: Int): Short = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[Short]
    }
  }

  override def getInt(columnIndex: Int): Int = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[Int]
    }
  }

  override def getLong(columnIndex: Int): Long = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any match {
        case i:Integer => i.longValue()
        case _ => any.asInstanceOf[Long]
      }
    }
  }

  override def getFloat(columnIndex: Int): Float = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[Float]
    }
  }

  override def getDouble(columnIndex: Int): Double = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[Double]
    }
  }

  override def getBigDecimal(columnIndex: Int, scale: Int): java.math.BigDecimal = {
    val mc = new MathContext(scale)
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[java.math.BigDecimal].round(mc)
    }
  }

  override def getBytes(columnIndex: Int): Array[Byte] = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[Array[Byte]]
    }
  }

  override def getDate(columnIndex: Int): Date = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[Date]
    }
  }

  override def getTime(columnIndex: Int): Time = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getTimestamp(columnIndex: Int): Timestamp = {
    val any  = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "Type is null")
    }else{
      any.asInstanceOf[Timestamp]
    }
  }

  override def getAsciiStream(columnIndex: Int): InputStream = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getUnicodeStream(columnIndex: Int): InputStream = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getBinaryStream(columnIndex: Int): InputStream = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getString(columnLabel: String): String = {
    getString(findColumn(columnLabel))
  }

  override def getBoolean(columnLabel: String): Boolean = {
    getBoolean(findColumn(columnLabel))
  }

  override def getByte(columnLabel: String): Byte = {
    getByte(findColumn(columnLabel))
  }

  override def getShort(columnLabel: String): Short = {
    getShort(findColumn(columnLabel))
  }

  override def getInt(columnLabel: String): Int = {
    getInt(findColumn(columnLabel))
  }

  override def getLong(columnLabel: String): Long = {
    getLong(findColumn(columnLabel))
  }

  override def getFloat(columnLabel: String): Float = {
    getFloat(findColumn(columnLabel))
  }

  override def getDouble(columnLabel: String): Double = {
    getDouble(findColumn(columnLabel))
  }

  override def getBigDecimal(columnLabel: String, scale: Int): java.math.BigDecimal = {
    getBigDecimal(findColumn(columnLabel), scale)
  }

  override def getBytes(columnLabel: String): Array[Byte] = {
    getBytes(findColumn(columnLabel))
  }

  override def getDate(columnLabel: String): Date = {
    getDate(findColumn(columnLabel))
  }

  override def getTime(columnLabel: String): Time = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getTimestamp(columnLabel: String): Timestamp = {
    getTimestamp(findColumn(columnLabel))
  }

  override def getAsciiStream(columnLabel: String): InputStream = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getUnicodeStream(columnLabel: String): InputStream = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getBinaryStream(columnLabel: String): InputStream = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getWarnings: SQLWarning = {
    warningChain
  }

  override def clearWarnings(): Unit = {
    warningChain = null
  }

  override def getCursorName: String = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getMetaData: UJESSQLResultSetMetaData = {
    if(metaData == null) init()
    resultSetMetaData
  }

  override def getObject(columnIndex: Int): Object = {
    val any = getColumnValue(columnIndex)
    if(wasNull()) {
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR,"Type is null")
    }else{
      any.asInstanceOf[Object]
    }
  }

  override def getObject(columnLabel: String): Object = {
    getObject(findColumn(columnLabel))
  }

  override def findColumn(columnLabel: String): Int = {
    var columnIndex  = -1
    var hasFindIndex : Boolean = false
    var num = 0
    for(column <- 1 to resultSetMetaData.getColumnCount if !hasFindIndex){
      num += 1
      if(resultSetMetaData.getColumnLabel(num).equals(columnLabel)){
        columnIndex = num
        num = 0
        hasFindIndex = true
      }
    }
    if(columnIndex == -1){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, s"can not find column: ${columnLabel}")
    }else columnIndex
  }

  override def getCharacterStream(columnIndex: Int): Reader = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getCharacterStream(columnLabel: String): Reader = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getBigDecimal(columnIndex: Int): java.math.BigDecimal = {
    getBigDecimal(columnIndex, BigDecimal.defaultMathContext.getPrecision)
  }

  override def getBigDecimal(columnLabel: String): java.math.BigDecimal = {
    getBigDecimal(findColumn(columnLabel))
  }

  override def isBeforeFirst: Boolean = {
    if(resultSetRow == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    }else currentRowCursor == -1
  }

  override def isAfterLast: Boolean = {
    if(resultSetRow == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    }else currentRowCursor > resultSetRow.size() - 1
  }

  override def isFirst: Boolean = {
    if(resultSetRow == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    }else currentRowCursor == 0
  }

  override def isLast: Boolean = {
    if(resultSetRow == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    }else currentRowCursor == resultSetRow.size() - 1
  }

  override def beforeFirst(): Unit = {
    if(resultSetRow == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    }else {
      currentRowCursor = -1
      updateCurrentRow(currentRowCursor)
    }
  }

  override def afterLast(): Unit = {
    if(resultSetRow == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    }else {
      currentRowCursor = resultSetRow.size()
      updateCurrentRow(currentRowCursor)
    }
  }

  override def first(): Boolean = {
    if(resultSetRow == null) false
    else{
      currentRowCursor = 0
      updateCurrentRow(currentRowCursor)
      true
    }
  }

  override def last(): Boolean = {
    if(resultSetRow == null) false
    else{
      currentRowCursor = resultSetRow.size() - 1
      updateCurrentRow(currentRowCursor)
      true
    }
  }

  override def getRow: Int = {
    if(resultSetRow == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    }else{
      currentRowCursor + 1
    }
  }

  override def absolute(row: Int): Boolean = {
    if(resultSetRow == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    }else if(row > resultSetRow.size()){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "The specified number of rows is greater than the maximum number of rows")
    }else {
      currentRowCursor = row match {
        case row if row > 0 => row + 1
        case row if row < 0 => resultSetRow.size() - math.abs(row) - 1
        case _ => -1
      }
      updateCurrentRow(currentRowCursor)
      true
    }
  }

  override def relative(rows: Int): Boolean = {
    if(resultSetRow == null){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_NULL)
    }else if(rows > resultSetRow.size()){
      throw new UJESSQLException(UJESSQLErrorCode.RESULTSET_ROWERROR, "The specified number of rows is greater than the maximum number of rows")
    }else {
      currentRowCursor = rows match {
        case rows if rows > 0 => currentRowCursor + rows
        case rows if rows < 0 => currentRowCursor - rows
        case _ => currentRowCursor
      }
      updateCurrentRow(currentRowCursor)
      true
    }
  }

  override def previous(): Boolean = {
    if(metaData == null) init()
    currentRowCursor -= 1
    updateCurrentRow(currentRowCursor)
    true
  }

  override def setFetchDirection(direction: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getFetchDirection: Int = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def setFetchSize(rows: Int): Unit = {
    fetchSizeNum = rows
  }

  override def getFetchSize: Int = {
    fetchSizeNum
  }

  override def getType: Int = {
    ResultSet.TYPE_SCROLL_INSENSITIVE
  }

  override def getConcurrency: Int = {
    ResultSet.CONCUR_READ_ONLY
  }

  override def rowUpdated(): Boolean = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def rowInserted(): Boolean = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def rowDeleted(): Boolean = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNull(columnIndex: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBoolean(columnIndex: Int, x: Boolean): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateByte(columnIndex: Int, x: Byte): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateShort(columnIndex: Int, x: Short): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateInt(columnIndex: Int, x: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateLong(columnIndex: Int, x: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateFloat(columnIndex: Int, x: Float): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateDouble(columnIndex: Int, x: Double): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBigDecimal(columnIndex: Int, x: java.math.BigDecimal): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateString(columnIndex: Int, x: String): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBytes(columnIndex: Int, x: Array[Byte]): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateDate(columnIndex: Int, x: Date): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateTime(columnIndex: Int, x: Time): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateTimestamp(columnIndex: Int, x: Timestamp): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateAsciiStream(columnIndex: Int, x: InputStream, length: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBinaryStream(columnIndex: Int, x: InputStream, length: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateCharacterStream(columnIndex: Int, x: Reader, length: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateObject(columnIndex: Int, x: scala.Any, scaleOrLength: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateObject(columnIndex: Int, x: scala.Any): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNull(columnLabel: String): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBoolean(columnLabel: String, x: Boolean): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateByte(columnLabel: String, x: Byte): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateShort(columnLabel: String, x: Short): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateInt(columnLabel: String, x: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateLong(columnLabel: String, x: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateFloat(columnLabel: String, x: Float): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateDouble(columnLabel: String, x: Double): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBigDecimal(columnLabel: String, x: java.math.BigDecimal): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateString(columnLabel: String, x: String): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBytes(columnLabel: String, x: Array[Byte]): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateDate(columnLabel: String, x: Date): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateTime(columnLabel: String, x: Time): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateTimestamp(columnLabel: String, x: Timestamp): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateAsciiStream(columnLabel: String, x: InputStream, length: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBinaryStream(columnLabel: String, x: InputStream, length: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateCharacterStream(columnLabel: String, reader: Reader, length: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateObject(columnLabel: String, x: scala.Any, scaleOrLength: Int): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateObject(columnLabel: String, x: scala.Any): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def insertRow(): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateRow(): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def deleteRow(): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def refreshRow(): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def cancelRowUpdates(): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def moveToInsertRow(): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def moveToCurrentRow(): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getStatement: Statement = {
    if(statement != null && !hasClosed)
      statement.asInstanceOf[Statement]
    else throw new UJESSQLException(UJESSQLErrorCode.STATEMENT_CLOSED)
  }

  override def getObject(columnIndex: Int, map: util.Map[String, Class[_]]): AnyRef = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getRef(columnIndex: Int): Ref = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getBlob(columnIndex: Int): Blob = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getClob(columnIndex: Int): Clob = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getArray(columnIndex: Int): sql.Array = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getObject(columnLabel: String, map: util.Map[String, Class[_]]): AnyRef = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getRef(columnLabel: String): Ref = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getBlob(columnLabel: String): Blob = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getClob(columnLabel: String): Clob = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getArray(columnLabel: String): sql.Array = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getDate(columnIndex: Int, cal: Calendar): Date = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getDate(columnLabel: String, cal: Calendar): Date = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getTime(columnIndex: Int, cal: Calendar): Time = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getTime(columnLabel: String, cal: Calendar): Time = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getTimestamp(columnIndex: Int, cal: Calendar): Timestamp = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getTimestamp(columnLabel: String, cal: Calendar): Timestamp = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getURL(columnIndex: Int): URL = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getURL(columnLabel: String): URL = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateRef(columnIndex: Int, x: Ref): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateRef(columnLabel: String, x: Ref): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBlob(columnIndex: Int, x: Blob): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBlob(columnLabel: String, x: Blob): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateClob(columnIndex: Int, x: Clob): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateClob(columnLabel: String, x: Clob): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateArray(columnIndex: Int, x: sql.Array): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateArray(columnLabel: String, x: sql.Array): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getRowId(columnIndex: Int): RowId = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getRowId(columnLabel: String): RowId = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateRowId(columnIndex: Int, x: RowId): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateRowId(columnLabel: String, x: RowId): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getHoldability: Int = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def isClosed: Boolean = {
    hasClosed
  }

  override def updateNString(columnIndex: Int, nString: String): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNString(columnLabel: String, nString: String): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNClob(columnIndex: Int, nClob: NClob): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNClob(columnLabel: String, nClob: NClob): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getNClob(columnIndex: Int): NClob = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getNClob(columnLabel: String): NClob = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getSQLXML(columnIndex: Int): SQLXML = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getSQLXML(columnLabel: String): SQLXML = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateSQLXML(columnIndex: Int, xmlObject: SQLXML): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateSQLXML(columnLabel: String, xmlObject: SQLXML): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getNString(columnIndex: Int): String = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getNString(columnLabel: String): String = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getNCharacterStream(columnIndex: Int): Reader = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getNCharacterStream(columnLabel: String): Reader = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNCharacterStream(columnIndex: Int, x: Reader, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNCharacterStream(columnLabel: String, reader: Reader, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateAsciiStream(columnIndex: Int, x: InputStream, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBinaryStream(columnIndex: Int, x: InputStream, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateCharacterStream(columnIndex: Int, x: Reader, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateAsciiStream(columnLabel: String, x: InputStream, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBinaryStream(columnLabel: String, x: InputStream, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateCharacterStream(columnLabel: String, reader: Reader, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBlob(columnIndex: Int, inputStream: InputStream, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBlob(columnLabel: String, inputStream: InputStream, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateClob(columnIndex: Int, reader: Reader, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateClob(columnLabel: String, reader: Reader, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNClob(columnIndex: Int, reader: Reader, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNClob(columnLabel: String, reader: Reader, length: Long): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNCharacterStream(columnIndex: Int, x: Reader): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNCharacterStream(columnLabel: String, reader: Reader): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateAsciiStream(columnIndex: Int, x: InputStream): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBinaryStream(columnIndex: Int, x: InputStream): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateCharacterStream(columnIndex: Int, x: Reader): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateAsciiStream(columnLabel: String, x: InputStream): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBinaryStream(columnLabel: String, x: InputStream): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateCharacterStream(columnLabel: String, reader: Reader): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBlob(columnIndex: Int, inputStream: InputStream): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateBlob(columnLabel: String, inputStream: InputStream): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateClob(columnIndex: Int, reader: Reader): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateClob(columnLabel: String, reader: Reader): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }
  override def updateNClob(columnIndex: Int, reader: Reader): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def updateNClob(columnLabel: String, reader: Reader): Unit = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getObject[T](columnIndex: Int, `type`: Class[T]): T = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def getObject[T](columnLabel: String, `type`: Class[T]): T = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def unwrap[T](iface: Class[T]): T = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }

  override def isWrapperFor(iface: Class[_]): Boolean = {
    throw new UJESSQLException(UJESSQLErrorCode.NOSUPPORT_RESULTSET)
  }
}
