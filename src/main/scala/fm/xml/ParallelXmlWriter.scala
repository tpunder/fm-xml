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

import fm.lazyseq.LazySeqBuilder
import java.io.{ByteArrayOutputStream, Closeable, OutputStream}
import javax.xml.bind.{JAXBContext, Marshaller}
import scala.reflect.{ClassTag, classTag}

final class ParallelXmlWriter[T: ClassTag](rootName: String, itemName: String, outputStream: OutputStream) extends Closeable {
  private[this] val jaxbContext: JAXBContext = JAXBContext.newInstance(classTag[T].runtimeClass)
  
  private[this] val encoding: String = "UTF-8"
  
  private[this] val marshaller: ThreadLocal[Marshaller] = new ThreadLocal[Marshaller] {
    override protected def initialValue: Marshaller = {
      val tmp = jaxbContext.createMarshaller()
      tmp.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
      tmp.setProperty(Marshaller.JAXB_FRAGMENT, true)
      tmp.setProperty(Marshaller.JAXB_ENCODING, encoding)
      tmp
    }
  }
  
  private def writeXml(xml: String): Unit = {
    outputStream.write(xml.getBytes(encoding))
  }
  
  private[this] val builder = new LazySeqBuilder[T]
  
  def write(item: T): Unit = {
    builder += item
  }
  
  builder.withConsumerThread { reader =>
    writeXml(s"<?xml version='1.0' encoding='$encoding'?>\n")
    writeXml(s"<$rootName>\n")
    
    reader.grouped(8).parMap{ items: IndexedSeq[T] =>
      val bos: ByteArrayOutputStream = new ByteArrayOutputStream
      val m: Marshaller = marshaller.get
      items.foreach { item =>
        m.marshal(item, bos)
      }
      bos
    }.foreach { xmlBytes: ByteArrayOutputStream =>
      xmlBytes.writeTo(outputStream)
    }
    
    writeXml(s"</$rootName>\n")
    outputStream.flush()
    outputStream.close()
  }
  
  def close(): Unit = {
    builder.close()
  }
}