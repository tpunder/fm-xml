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

import fm.common.{FileUtil, InputStreamResource, Resource, SingleUseResource}
import java.io.{File, InputStream, InputStreamReader, OutputStream, Reader, StringReader}
import scala.reflect.{ClassTag, classTag}

final case class XmlReaderWriter[T: ClassTag](rootName: String, itemName: String, defaultNamespaceURI: String = "", overrideDefaultNamespaceURI: String = "") {
  private[this] val classes: Seq[Class[_]] = Seq(classTag[T].runtimeClass)
  
  def reader(f: File)               : XmlReader[T] = reader(InputStreamResource.forFileOrResource(f))
  def reader(is: InputStream)       : XmlReader[T] = reader(InputStreamResource.forInputStream(is))
  def reader(r: InputStreamResource): XmlReader[T] = reader(r.reader())
  def reader(s: String)             : XmlReader[T] = reader(new StringReader(s))
  def reader(r: Reader)             : XmlReader[T] = reader(SingleUseResource(r))
  def reader(r: Resource[Reader])   : XmlReader[T] = new XmlReader(rootName, itemName, defaultNamespaceURI, overrideDefaultNamespaceURI, r)
  
  def write(f: File)(fun: XmlWriter => Unit) {
    FileUtil.writeFile(f, true){ os => write(os)(fun) }
  }
  
  def write(os: OutputStream)(fun: XmlWriter => Unit) {
    val writer = new XmlWriter(classes, rootName, defaultNamespaceURI, os)
    fun(writer)
    writer.close()
  }
  
  def writer(os: OutputStream): XmlWriter = new XmlWriter(classes, rootName, defaultNamespaceURI, os)
  
  def parallelWriter(os: OutputStream): ParallelXmlWriter[T] = new ParallelXmlWriter(rootName, itemName, os)
}
