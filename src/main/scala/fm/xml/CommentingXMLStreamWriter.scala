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

object CommentingXMLStreamWriter {
  private sealed trait Command { def apply(writer: XMLStreamWriter): Unit }
  private sealed trait StartElement extends Command { def localName: String }
  private sealed trait EmptyElement extends StartElement
  private sealed trait Attribute extends Command { def localName: String; def value: String }
  private sealed trait Text extends Command { def data: String }
  
  private case class Namespace(prefix: String, namespaceURI: String) extends Command {
    def apply(writer: XMLStreamWriter): Unit = writer.writeNamespace(prefix, namespaceURI)
  }
  
  private object StartElement {
    def apply(localName: String): StartElement1 = StartElement1(localName)
    def apply(namespaceURI: String, localName: String): StartElement2 = StartElement2(namespaceURI, localName)
    def apply(prefix: String, localName: String, namespaceURI: String): StartElement3 = StartElement3(prefix, localName, namespaceURI)
  }
  
  private object EmptyElement {
    def apply(localName: String): EmptyElement1 = EmptyElement1(localName)
    def apply(namespaceURI: String, localName: String): EmptyElement2 = EmptyElement2(namespaceURI, localName)
    def apply(prefix: String, localName: String, namespaceURI: String): EmptyElement3 = EmptyElement3(prefix, localName, namespaceURI)
  }
  
  private object Attribute {
    def apply(localName: String, value: String): Attribute2 = Attribute2(localName, value)
    def apply(namespaceURI: String, localName: String, value: String): Attribute3 = Attribute3(namespaceURI, localName, value)
    def apply(prefix: String, namespaceURI: String, localName: String, value: String): Attribute4 = Attribute4(prefix, namespaceURI, localName, value)
  }
  
  private object Chars {
    def apply(text: Array[Char], start: Int, length: Int): Chars = Chars(new String(text, start, length))
  }
  
  private case class Chars(data: String) extends Text {
    def apply(writer: XMLStreamWriter): Unit = writer.writeCharacters(data)
  }
  
  private case class CData(data: String) extends Text {
    def apply(writer: XMLStreamWriter): Unit = writer.writeCData(data)
  } 
  
  private case class StartElement1(localName: String) extends StartElement {
    def apply(writer: XMLStreamWriter): Unit = writer.writeStartElement(localName)
  }
  
  private case class StartElement2(namespaceURI: String, localName: String) extends StartElement {
    def apply(writer: XMLStreamWriter): Unit = writer.writeStartElement(namespaceURI, localName)
  }
  
  private case class StartElement3(prefix: String, localName: String, namespaceURI: String) extends StartElement {
    def apply(writer: XMLStreamWriter): Unit = writer.writeStartElement(prefix, localName, namespaceURI)
  }
  
  private case class EmptyElement1(localName: String) extends EmptyElement {
    def apply(writer: XMLStreamWriter): Unit = writer.writeEmptyElement(localName)
  }
  
  private case class EmptyElement2(namespaceURI: String, localName: String) extends EmptyElement {
    def apply(writer: XMLStreamWriter): Unit = writer.writeEmptyElement(namespaceURI, localName)
  }
  
  private case class EmptyElement3(prefix: String, localName: String, namespaceURI: String) extends EmptyElement {
    def apply(writer: XMLStreamWriter): Unit = writer.writeEmptyElement(prefix, localName, namespaceURI)
  }
  
  private case class Attribute2(localName: String, value: String) extends Attribute {
    def apply(writer: XMLStreamWriter): Unit = writer.writeAttribute(localName, value)
  }
  
  private case class Attribute3(namespaceURI: String, localName: String, value: String) extends Attribute {
    def apply(writer: XMLStreamWriter): Unit = writer.writeAttribute(namespaceURI, localName, value)
  }
  
  private case class Attribute4(prefix: String, namespaceURI: String, localName: String, value: String) extends Attribute {
    def apply(writer: XMLStreamWriter): Unit = writer.writeAttribute(prefix, namespaceURI, localName, value)
  }
  
  private case object EndElement extends Command {
    def apply(writer: XMLStreamWriter): Unit = writer.writeEndElement()
  }
}

final case class CommentingXMLStreamWriter(protected val self: IndentingXMLStreamWriter, comments: XMLCommentProvider) extends XMLStreamWriterProxy {
  import CommentingXMLStreamWriter._
  
  // We buffer at most 1 complete element (start tag, attributes, chars/cdata, end tag)
  private[this] val buffer: ArrayBuffer[Command] = new ArrayBuffer()
  
  private[this] val localNames: ArrayBuffer[String] = new ArrayBuffer()
  
  private def push(localName: String): Unit = localNames += localName
  private def pop(): Unit = localNames.remove(localNames.size - 1)
  
  protected def stackKey(): String = localNames.mkString(".")
  
  /**
   * This is called before the start element is written
   */
  protected def beforeStartElement(localName: String): Unit = {
    flushBuffer()
    push(localName)
  }
  
  /**
   * This is called after the closing element tag is written
   */
  protected def afterEndElement(): Unit = {
    flushBuffer()
    pop()
  }
  
  protected def flushBuffer(): Unit = {
    if (buffer.size == 0) return
    
    // TODO: add stricter checking.  Should be StartElement, optional Attributes, optional Text, and optional EndElement
    //val localName: String = buffer.collect{ case start: StartElement => start.localName }.head
    val localNamePath: String = stackKey()
    val attributes: Map[String, String] = buffer.collect { case attr: Attribute => (attr.localName, attr.value) }.toMap
    val value: Option[String] = buffer.collect{ case txt: Text => txt.data }.mkString("").toBlankOption
    
    // Comments before the start element
    comments.leadingComment(localNamePath, attributes, value).foreach{ comment: String =>
      self.writeIndentedComment(comment.requireLeading(" ").requireTrailing(" "))
    }
    
    buffer.foreach{ command: Command => command(self) }
    
    // Comments after the end element (or the start element if there are nested elements under this one)
    comments.trailingComment(localNamePath, attributes, value).foreach{ comment: String =>
      self.writeCharacters(" ")
      self.writeComment(comment.requireLeading(" ").requireTrailing(" "))
    }
    
    buffer.clear()
  }
  
  
  //
  // These are all the XMLStreamWriter overrides that hook into handleStartElement/handleEndElement:
  //
  
  override def writeEmptyElement(localName: String): Unit = {
    beforeStartElement(localName)
    buffer += EmptyElement(localName)
    afterEndElement()
  }
  
  override def writeEmptyElement(prefix: String, localName: String, namespaceURI: String): Unit = {
    beforeStartElement(localName)
    buffer += EmptyElement(prefix, localName, namespaceURI)
    afterEndElement()
  }
  
  override def writeEmptyElement(namespaceURI: String, localName: String): Unit = {
    beforeStartElement(localName)
    buffer += EmptyElement(namespaceURI, localName)
    afterEndElement()
  }
  
  override def writeStartElement(prefix: String, localName: String, namespaceURI: String): Unit = {
    beforeStartElement(localName)
    buffer += StartElement(prefix, localName, namespaceURI)
  }
  
  override def writeStartElement(namespaceURI: String, localName: String): Unit = {
    beforeStartElement(localName)
    buffer += StartElement(namespaceURI, localName)
  }
  
  override def writeStartElement(localName: String): Unit = {
    beforeStartElement(localName)
    buffer += StartElement(localName)
  }
  
  override def writeEndElement(): Unit = {
    buffer += EndElement
    afterEndElement()
  }
  
  override def writeAttribute(namespaceURI: String, localName: String, value: String): Unit = {
    buffer += Attribute(namespaceURI, localName, value)
  }
  
  override def writeAttribute(prefix: String, namespaceURI: String, localName: String, value: String): Unit = {
    buffer += Attribute(prefix, namespaceURI, localName, value)
  }
  
  override def writeAttribute(localName: String, value: String): Unit = {
    buffer += Attribute(localName, value)
  }
  
  override def writeCData(data: String): Unit = {
    buffer += CData(data)
  }
  
  override def writeCharacters(text: Array[Char], start: Int, length: Int): Unit = {
    buffer += Chars(text, start, length)
  }
  
  override def writeCharacters(text: String): Unit = {
    buffer += Chars(text)
  }
  
  override def writeEndDocument(): Unit = {
    flushBuffer()
    self.writeEndDocument()
  }
  
  override def setPrefix(prefix: String, uri: String): Unit = {
    //println(s"setPrefix($prefix, $uri)")
    self.setPrefix(prefix, uri)
  }
  
  override def writeDTD(dtd: String): Unit = self.writeDTD(dtd)
  
  override def writeDefaultNamespace(namespaceURI: String): Unit = {
    //println(s"writeDefaultNamespace($namespaceURI)")
    self.writeDefaultNamespace(namespaceURI)
  }
  
  override def writeEntityRef(name: String): Unit = {
    //println(s"writeEntityRef($name)")
    self.writeEntityRef(name)
  }
  
  override def writeNamespace(prefix: String, namespaceURI: String): Unit = {
    buffer += Namespace(prefix, namespaceURI)
  }
  
  override def writeProcessingInstruction(target: String, data: String): Unit = {
    //println(s"writeProcessingInstruction($target, $data)")
    self.writeProcessingInstruction(target, data)
  }
  
  override def writeProcessingInstruction(target: String): Unit = {
    //println(s"writeProcessingInstruction($target)")
    self.writeProcessingInstruction(target)
  }
}