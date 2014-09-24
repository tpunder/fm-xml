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

import javax.xml.stream.XMLStreamWriter

final case class IndentingXMLStreamWriter(protected val self: XMLStreamWriter, indent: String = "  ") extends XMLStreamWriterProxy {  
  private[this] var depth: Int = 0
  private[this] var lastElementWasStartElement: Boolean = false
  
  private def onStartElement(): Unit = {
    if (depth > 0) super.writeCharacters("\n")
    doIndent()
    depth += 1
    lastElementWasStartElement = true
  }
  
  private def onEndElement(): Unit = {
    depth -= 1
    if (lastElementWasStartElement) {
      // We don't indent since the start tag for this is on the same line
      lastElementWasStartElement = false
    } else {
      // We only indent if the last written tag was not a start element
      super.writeCharacters("\n")
      doIndent()
    }
  }
  
  private def onEmptyElement(): Unit = {
    lastElementWasStartElement = false
    if (depth > 0) super.writeCharacters("\n")
    doIndent()
  }
  
  private def doIndent(): Unit = {
    if (depth > 0) {
      var i: Int = 0
      while (i < depth) {
        super.writeCharacters(indent)
        i += 1
      }
    }
  }
  
  /**
   * Write an indented comment so that is lines up with the next start element tag
   */
  def writeIndentedComment(data: String): Unit = {
    if (depth > 0) super.writeCharacters("\n")
    doIndent()
    super.writeComment(data)
    if (depth == 0) super.writeCharacters("\n")
  }
  
  override def writeStartDocument(): Unit = {
    super.writeStartDocument()
    super.writeCharacters("\n")
  }
  
  override def writeStartDocument(version: String): Unit = {
    super.writeStartDocument(version)
    super.writeCharacters("\n")
  }
  
  override def writeStartDocument(encoding: String, version: String): Unit = {
    super.writeStartDocument(encoding, version)
    super.writeCharacters("\n")
  }
  
  override def writeStartElement(localName: String): Unit = {
    onStartElement()
    super.writeStartElement(localName)
  }
  
  override def writeStartElement(namespaceURI: String, localName: String): Unit = {
    onStartElement()
    super.writeStartElement(namespaceURI, localName)
  }
  
  override def writeStartElement(prefix: String, localName: String, namespaceURI: String): Unit = {
    onStartElement()
    super.writeStartElement(prefix, localName, namespaceURI)
  }
  
  override def writeEmptyElement(localName: String): Unit = {
    onEmptyElement()
    super.writeEmptyElement(localName)
  }
  
  override def writeEmptyElement(prefix: String, localName: String, namespaceURI: String): Unit = {
    onEmptyElement()
    super.writeEmptyElement(prefix, localName, namespaceURI)
  }
  
  override def writeEmptyElement(namespaceURI: String, localName: String): Unit = {
    onEmptyElement()
    super.writeEmptyElement(namespaceURI, localName)
  }
  
  override def writeEndElement(): Unit = {
    onEndElement()
    super.writeEndElement()
  }
}