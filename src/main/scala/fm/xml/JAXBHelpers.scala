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

import scala.reflect.ClassTag

class JAXBHelpers[T: ClassTag](
  packageName: String,
  rootElement: String,
  itemPath: String,
  defaultNamespaceURI: String = "",
  overrideDefaultNamespaceURI: String = "",
  fragment: Boolean = true,
  format: Boolean = true,
  indent: String = "  "
) {

  final val xmlReaderWriter: XmlReaderWriter[T] = XmlReaderWriter(rootElement, itemPath, defaultNamespaceURI = defaultNamespaceURI, overrideDefaultNamespaceURI = overrideDefaultNamespaceURI)
  
  final val marshaller: JAXBMarshaller[T] = new JAXBMarshaller[T](packageName = packageName, rootElement = rootElement, fragment = fragment, format = format, indent = indent)
  
  final def toXML(obj: T): String = marshaller.toXML(obj)
  final def toXML(obj: T, comments: XMLComments): String = marshaller.toXML(obj, comments)
  
  final def fromXML(xml: String): T = marshaller.fromXML(xml)
}
