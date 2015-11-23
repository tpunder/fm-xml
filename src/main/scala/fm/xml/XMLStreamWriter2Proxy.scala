/*
 * Copyright 2015 Frugal Mechanic (http://frugalmechanic.com)
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
package fm.xml

import org.codehaus.stax2.XMLStreamWriter2

trait XMLStreamWriter2Proxy extends XMLStreamWriter2 with XMLStreamWriterProxy {
  protected def self: XMLStreamWriter2
  
  // Members declared in org.codehaus.stax2.XMLStreamWriter2
  def closeCompletely(): Unit = self.closeCompletely()
  def copyEventFromReader(r: org.codehaus.stax2.XMLStreamReader2, preserveEventData: Boolean): Unit = self.copyEventFromReader(r, preserveEventData)
  def getEncoding(): String = self.getEncoding()
  def getLocation(): org.codehaus.stax2.XMLStreamLocation2 = self.getLocation()
  def isPropertySupported(name: String): Boolean = self.isPropertySupported(name)
  def setProperty(name: String, value: Any): Boolean = self.setProperty(name, value)
  def writeCData(text: Array[Char], start: Int, len: Int): Unit = self.writeCData(text, start, len)
  def writeDTD(rootName: String, systemId: String, publicId: String, internalSubset: String): Unit = self.writeDTD(rootName, systemId, publicId, internalSubset)
  def writeFullEndElement(): Unit = self.writeFullEndElement()
  def writeRaw(text: Array[Char], offset: Int, length: Int): Unit = self.writeRaw(text, offset, length)
  def writeRaw(text: String, offset: Int, length: Int): Unit = self.writeRaw(text, offset, length)
  def writeRaw(text: String): Unit = self.writeRaw(text)
  def writeSpace(text: Array[Char], offset: Int, length: Int): Unit = self.writeSpace(text, offset, length)
  def writeSpace(text: String): Unit = self.writeSpace(text)
  def writeStartDocument(version: String, encoding: String, standAlone: Boolean): Unit = self.writeStartDocument(version, encoding, standAlone)
  
  // Members declared in org.codehaus.stax2.validation.Validatable
  def setValidationProblemHandler(h: org.codehaus.stax2.validation.ValidationProblemHandler): org.codehaus.stax2.validation.ValidationProblemHandler = self.setValidationProblemHandler(h)
  def stopValidatingAgainst(validator: org.codehaus.stax2.validation.XMLValidator): org.codehaus.stax2.validation.XMLValidator = self.stopValidatingAgainst(validator)
  def stopValidatingAgainst(schema: org.codehaus.stax2.validation.XMLValidationSchema): org.codehaus.stax2.validation.XMLValidator = self.stopValidatingAgainst(schema)
  def validateAgainst(schema: org.codehaus.stax2.validation.XMLValidationSchema): org.codehaus.stax2.validation.XMLValidator = self.validateAgainst(schema)
  
  // Members declared in org.codehaus.stax2.typed.TypedXMLStreamWriter
  def writeBinary(variant: org.codehaus.stax2.typed.Base64Variant, value: Array[Byte], from: Int, length: Int): Unit = self.writeBinary(variant, value, from, length)
  def writeBinary(value: Array[Byte], from: Int, length: Int): Unit = self.writeBinary(value, from, length)
  def writeBinaryAttribute(variant: org.codehaus.stax2.typed.Base64Variant, prefix: String, namespaceURI: String, localName: String, value: Array[Byte]): Unit = self.writeBinaryAttribute(variant, prefix, namespaceURI, localName, value)
  def writeBinaryAttribute(prefix: String, namespaceURI: String, localName: String, value: Array[Byte]): Unit = self.writeBinaryAttribute(prefix, namespaceURI, localName, value)
  def writeBoolean(value: Boolean): Unit = self.writeBoolean(value)
  def writeBooleanAttribute(prefix: String, namespaceURI: String, localName: String, value: Boolean): Unit = self.writeBooleanAttribute(prefix, namespaceURI, localName, value)
  def writeDecimal(value: java.math.BigDecimal): Unit = self.writeDecimal(value)
  def writeDecimalAttribute(prefix: String, namespaceURI: String, localName: String, value: java.math.BigDecimal): Unit = self.writeDecimalAttribute(prefix, namespaceURI, localName, value)
  def writeDouble(value: Double): Unit = self.writeDouble(value)
  def writeDoubleArray(value: Array[Double], from: Int, length: Int): Unit = self.writeDoubleArray(value, from, length)
  def writeDoubleArrayAttribute(prefix: String, namespaceURI: String, localName: String, value: Array[Double]): Unit = self.writeDoubleArrayAttribute(prefix, namespaceURI, localName, value)
  def writeDoubleAttribute(prefix: String, namespaceURI: String, localName: String, value: Double): Unit = self.writeDoubleAttribute(prefix, namespaceURI, localName, value)
  def writeFloat(value: Float): Unit = self.writeFloat(value)
  def writeFloatArray(value: Array[Float], from: Int, length: Int): Unit = self.writeFloatArray(value, from, length)
  def writeFloatArrayAttribute(prefix: String, namespaceURI: String, localName: String, value: Array[Float]): Unit = self.writeFloatArrayAttribute(prefix, namespaceURI, localName, value)
  def writeFloatAttribute(prefix: String, namespaceURI: String, localName: String, value: Float): Unit = self.writeFloatAttribute(prefix, namespaceURI, localName, value)
  def writeInt(value: Int): Unit = self.writeInt(value)
  def writeIntArray(value: Array[Int], from: Int, length: Int): Unit = self.writeIntArray(value, from, length)
  def writeIntArrayAttribute(prefix: String, namespaceURI: String, localName: String, value: Array[Int]): Unit = self.writeIntArrayAttribute(prefix, namespaceURI, localName, value)
  def writeIntAttribute(prefix: String, namespaceURI: String, localName: String, value: Int): Unit = self.writeIntAttribute(prefix, namespaceURI, localName, value)
  def writeInteger(value: java.math.BigInteger): Unit = self.writeInteger(value)
  def writeIntegerAttribute(prefix: String, namespaceURI: String, localName: String, value: java.math.BigInteger): Unit = self.writeIntegerAttribute(prefix, namespaceURI, localName, value)
  def writeLong(value: Long): Unit = self.writeLong(value)
  def writeLongArray(value: Array[Long], from: Int, length: Int): Unit = self.writeLongArray(value, from, length)
  def writeLongArrayAttribute(prefix: String, namespaceURI: String, localName: String, value: Array[Long]): Unit = self.writeLongArrayAttribute(prefix, namespaceURI, localName, value)
  def writeLongAttribute(prefix: String, namespaceURI: String, localName: String, value: Long): Unit = self.writeLongAttribute(prefix, namespaceURI, localName, value)
  def writeQName(value: javax.xml.namespace.QName): Unit = self.writeQName(value)
  def writeQNameAttribute(prefix: String, namespaceURI: String, localName: String, value: javax.xml.namespace.QName): Unit = self.writeQNameAttribute(prefix, namespaceURI, localName, value)
  
}