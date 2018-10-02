/*
 * Copyright 2018 Frugal Mechanic (http://frugalmechanic.com)
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
import fm.common.Logging
import javax.xml.bind.{JAXBContext, Unmarshaller}
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import org.codehaus.stax2.XMLStreamReader2
import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}

object XmlReaderPath {
  // Apply helper for reading a non-mapped value
  def apply[XmlValue: ClassTag](itemPath: String): XmlReaderPath[XmlValue, XmlValue] = XmlReaderPath[XmlValue, XmlValue](itemPath, identity)
}

/**
  *
  * @param itemPath The XPath-like path to the element we are interested in.  (e.g. "part", "items/part", etc)
  * @param toMappedValue Map the XmlValue into another return type, this should match the return type in MultiXmlReader
  * @tparam XmlValue The unmarshalled element class type
  * @tparam MappedXmlValue A mapped XmlValue type
  */
final case class XmlReaderPath[XmlValue: ClassTag, MappedXmlValue](
  itemPath: String,
  toMappedValue: XmlValue => MappedXmlValue
) extends Logging {
  // Make this public, so we can use it for the XmlWriter JAXBContext
  private[this] val itemClass: Class[XmlValue] = classTag[XmlValue].runtimeClass.asInstanceOf[Class[XmlValue]]
  private[this] val jaxbContext: JAXBContext = JAXBContext.newInstance(itemClass)
  private[this] val unmarshaller: Unmarshaller = jaxbContext.createUnmarshaller()

  // The XPath-like path to the element we are interested in
  // part => Array("part"), items/part => Array("items","part")
  // This is reversed for easy comparison to the ArrayStack we use
  private[xml] val path: Array[String] = itemPath.split('/')

  // The name of the element we care about (last part of the path)
  // items/part => part
  private[xml] val itemName: String = path.last

  private[xml] val targetDepth: Int = path.length - 1

  private[xml] def readValue(xmlStreamReader: XMLStreamReader2): MappedXmlValue =  {
    xmlStreamReader.require(START_ELEMENT, null, itemName)

    val value: XmlValue = unmarshaller.unmarshal(xmlStreamReader, itemClass).getValue
    toMappedValue(value)
  }

  // Check to see if the target depth and path match
  private[xml] def elementMatches(xmlStreamReader: XMLStreamReader2, currentDepth: Int): Boolean = {
    currentDepth == targetDepth && xmlStreamReader.getLocalName === itemName
  }

  private[xml] def pathMatches(xmlStreamReader: XMLStreamReader2, currentPath: mutable.ArrayStack[String]): Boolean = {
    if (currentPath.length > targetDepth) return false
    if (currentPath.length === targetDepth && xmlStreamReader.getLocalName =!= itemName) return false

    // Check the previous paths
    var depth: Int = 0
    while (depth < currentPath.length) {

      // The stack indexes (in parens) change as the new paths get added
      // items(0)                                     - current depth 1
      // items(1) / items2(0)                         - current depth 2
      // items(2) / items2(1) / items3 (0)            - current depth 3
      // items(4) / items2(2) / items3 (1) / part (0) - current depth 4

      // The path is static: items(0) / items2(1) / items3 (2) / part (3)

      // When currentPath length is 3, the first item in the currentPath is (currentPath.length - 1), each iteration minus another 1
      if (currentPath(currentPath.length - 1 - depth) =!= path(depth)) return false

      depth += 1
    }

    true
  }
}