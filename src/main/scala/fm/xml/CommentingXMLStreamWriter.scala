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

import fm.common.Implicits._
import javax.xml.stream.XMLStreamWriter
import scala.collection.mutable.ArrayBuffer

final case class CommentingXMLStreamWriter(protected val self: IndentingXMLStreamWriter, comments: XMLComments) extends XMLStreamWriterProxy {
  
  private[this] val localNames: ArrayBuffer[String] = new ArrayBuffer()
  
  private def push(localName: String): Unit = localNames += localName
  private def pop(): Unit = localNames.remove(localNames.size - 1)
  
  protected def stackKey(): String = localNames.mkString(".")
  
  /**
   * This is called before the start element is written
   */
  protected def handleStartElement(localName: String): Unit = {
    push(localName)
    
    val comment: String = comments.leading.getOrElse(stackKey(), "")
    
    if (comment.isNotBlank) self.writeIndentedComment(comment.requireLeading(" ").requireTrailing(" "))
  }
  
  /**
   * This is called after the closing element tag is written
   */
  protected def handleEndElement(): Unit = {
    val comment: String = comments.trailing.getOrElse(stackKey(), "")
    
    if (comment.isNotBlank) {
      self.writeCharacters(" ")
      self.writeComment(comment.requireLeading(" ").requireTrailing(" "))
    }
    
    pop()
  }
  
  
  //
  // These are all the XMLStreamWriter overrides that hook into handleStartElement/handleEndElement:
  //
  
  override def writeEmptyElement(localName: String): Unit = {
    handleStartElement(localName)
    super.writeEmptyElement(localName)
    handleEndElement()
  }
  
  override def writeEmptyElement(prefix: String, localName: String, namespaceURI: String): Unit = {
    handleStartElement(localName)
    super.writeEmptyElement(prefix, localName, namespaceURI)
    handleEndElement()
  }
  
  override def writeEmptyElement(namespaceURI: String, localName: String): Unit = {
    handleStartElement(localName)
    super.writeEmptyElement(namespaceURI, localName)
    handleEndElement()
  }
  
  override def writeStartElement(prefix: String, localName: String, namespaceURI: String): Unit = {
    handleStartElement(localName)
    super.writeStartElement(prefix, localName, namespaceURI)
  }
  
  override def writeStartElement(namespaceURI: String, localName: String): Unit = {
    handleStartElement(localName)
    super.writeStartElement(namespaceURI, localName)
  }
  
  override def writeStartElement(localName: String): Unit = {
    handleStartElement(localName)
    super.writeStartElement(localName)
  }
  
  override def writeEndElement(): Unit = {
    super.writeEndElement()
    handleEndElement()
  }
}