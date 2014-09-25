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

import scala.reflect.{ClassTag, classTag}

@deprecated("Use fm.xml.JAXBHelpers instead","")
class Jaxb2Helpers[T: ClassTag](packageName: String, rootElement: String, itemPath: String, defaultNamespaceURI: String = "", overrideDefaultNamespaceURI: String = "") {
  private[this] val helpers: JAXBHelpers[T] = new JAXBHelpers[T](packageName = packageName, rootElement = rootElement, itemPath = itemPath, defaultNamespaceURI = defaultNamespaceURI, overrideDefaultNamespaceURI = overrideDefaultNamespaceURI, fragment = true, format = true, indent = "    ")
  
  def toXml(obj: T): String = helpers.toXML(obj)
  def fromXml(xml: String): T = helpers.fromXML(xml)
  
  def xmlReaderWriter: XmlReaderWriter[T] = helpers.xmlReaderWriter
}
