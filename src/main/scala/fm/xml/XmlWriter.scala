/*
 * Copyright 2014 Frugal Mechanic (http://frugalmechanic.com)
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

import com.ctc.wstx.stax.WstxOutputFactory
import fm.common.Implicits._
import java.io.{Closeable, OutputStream}
import javax.xml.bind.{JAXBContext, Marshaller}
import org.codehaus.stax2.XMLStreamWriter2
import org.codehaus.stax2.validation.Validatable

final class XmlWriter(classes: Seq[Class[_]], rootName: String, defaultNamespace: String, outputStream: OutputStream) extends Closeable with Validatable {
  private[this] val jaxbContext: JAXBContext = JAXBContext.newInstance(classes: _*)
  
  private[this] val encoding: String = "UTF-8"
  
  private[this] val marshaller: Marshaller = jaxbContext.createMarshaller()
  marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
  marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true)
  marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding)
  
  private[this] val outputFactory: WstxOutputFactory = new WstxOutputFactory()
  outputFactory.configureForSpeed()
  
  private[this] val xmlStreamWriter2: XMLStreamWriter2 = outputFactory.createXMLStreamWriter(outputStream, encoding).asInstanceOf[XMLStreamWriter2]  
  private[this] val xmlStreamWriter: IndentingXMLStreamWriter = new IndentingXMLStreamWriter(xmlStreamWriter2)
  
  // If we are validating we need to be able to hookup validation before we start writing
  private lazy val init: Unit = {
    xmlStreamWriter.writeStartDocument(encoding, "1.0")
    
    if (defaultNamespace.isNotNullOrBlank) {
      xmlStreamWriter.setDefaultNamespace(defaultNamespace)
      xmlStreamWriter.writeStartElement(defaultNamespace, rootName)
      xmlStreamWriter.writeDefaultNamespace(defaultNamespace)
    } else {
      xmlStreamWriter.writeStartElement(rootName)
    }
  }
  
  // To be used with care
  def writeStartElement(name: String): Unit = {
    init
    
    // Need to use the defaultNamespace so validation works
    if (defaultNamespace.isNotNullOrBlank) xmlStreamWriter.writeStartElement(defaultNamespace, name)
    else xmlStreamWriter.writeStartElement(name)
  }

  // To be used with care
  def writeEndElement(): Unit = {
    init
    xmlStreamWriter.writeEndElement()
  }

  def writeWrapperElement(name: String) (f: => Unit): Unit = {
    writeStartElement(name)
    f
    writeEndElement()
  }
  
  def write(item: AnyRef): Unit = {
    init
    marshaller.marshal(item, xmlStreamWriter)
  }
  
  def close(): Unit = {
    init
    xmlStreamWriter.writeEndElement() // could do writeEndDocument but that's probably sloppy
    xmlStreamWriter.flush()
    xmlStreamWriter.close() // This does not close the underlying OutputStream
    outputStream.flush()
    outputStream.close()
  }
  
  //
  // Validatable implementation
  //
  import org.codehaus.stax2.validation._
  
  private def validatable: Validatable = xmlStreamWriter2
  
  def setValidationProblemHandler(h: ValidationProblemHandler): ValidationProblemHandler = validatable.setValidationProblemHandler(h)
  def stopValidatingAgainst(schema: XMLValidationSchema): XMLValidator = validatable.stopValidatingAgainst(schema)
  def stopValidatingAgainst(validator: XMLValidator): XMLValidator = validatable.stopValidatingAgainst(validator)
  def validateAgainst(schema: XMLValidationSchema): XMLValidator = validatable.validateAgainst(schema)
}