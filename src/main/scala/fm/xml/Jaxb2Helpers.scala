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

class Jaxb2Helpers[T: ClassTag](packageName: String, rootElement: String, itemPath: String, defaultNamespaceURI: String = "", overrideDefaultNamespaceURI: String = "") {
  private[this] val jaxb2Marshaller: ThreadLocal[Jaxb2Marshaller] = new ThreadLocal[Jaxb2Marshaller] {
    override protected def initialValue: Jaxb2Marshaller = Jaxb2Marshaller(packageName).fragment.pretty
  }
  
  def withJaxb2Marshaller[X](f: Jaxb2Marshaller => X): X = f(jaxb2Marshaller.get)

  def toXml(fp: T): String = withJaxb2Marshaller(_.toXml(fp))
  def fromXml(xml: String): T = withJaxb2Marshaller(_.fromXml[T](xml))
  
  val xmlReaderWriter: XmlReaderWriter[T] = XmlReaderWriter(rootElement, itemPath, defaultNamespaceURI = defaultNamespaceURI, overrideDefaultNamespaceURI = overrideDefaultNamespaceURI)
}
