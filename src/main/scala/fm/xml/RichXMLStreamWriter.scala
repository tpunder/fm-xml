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


import javax.xml.stream.XMLStreamConstants._
import javax.xml.stream.{XMLStreamException, XMLStreamWriter}
import scala.annotation.{tailrec, switch}

object RichXMLStreamWriter {
  // This cannot be in Implicits since it's an optional dependency
  implicit def toRichXMLStreamWriter(sw: XMLStreamWriter): RichXMLStreamWriter = new RichXMLStreamWriter(sw)
}

final class RichXMLStreamWriter(val sw: XMLStreamWriter) extends AnyVal {
  def writeElement(localName: String)(f: => Unit): Unit = {
    sw.writeStartElement(localName)
    f
    sw.writeEndElement()
  }
  
  def writeElement(namespaceURI: String, localName: String)(f: => Unit): Unit = {
    sw.writeStartElement(namespaceURI, localName)
    f
    sw.writeEndElement()
  }
  
  def writeElement(prefix: String, localName: String, namespaceURI: String)(f: => Unit): Unit = {
    sw.writeStartElement(prefix, localName, namespaceURI)
    f
    sw.writeEndElement()
  }
}