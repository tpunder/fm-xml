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

import fm.common.{ClassUtil, Logging, Resource}
import java.io.{Closeable, InputStream, OutputStream, Reader, StringReader, StringWriter, Writer}
import javax.xml.bind.{JAXBContext, Marshaller, Unmarshaller}
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.stream.{XMLInputFactory, XMLOutputFactory, XMLStreamReader, XMLStreamWriter}
import scala.reflect.{ClassTag, classTag}

object JAXBMarshaller {
  private val inputFactory: XMLInputFactory = XMLInputFactory.newInstance()
  private val outputFactory: XMLOutputFactory = XMLOutputFactory.newInstance()
  
  // XMLStreamWriter doesn't implement AutoCloseable or Closeable so we need a rich wrapper for Resource.using to work
  private final implicit class XMLStreamWriterCloseable(writer: XMLStreamWriter) extends Closeable { def close(): Unit = writer.close() }
  
  // XMLStreamReader doesn't implement AutoCloseable or Closeable so we need a rich wrapper for Resource.using to work
  private final implicit class XMLStreamReaderCloseable(reader: XMLStreamReader) extends Closeable { def close(): Unit = reader.close() }
}

/**
 * A wraper around the JAXB Marshaller/Unmarshaller.  This class is thread-safe.
 */
final class JAXBMarshaller[T: ClassTag](
  packageName: String,
  rootElement: String,
  fragment: Boolean = true,
  format: Boolean = true,
  indent: String = "  "
) extends Logging {
  
  import JAXBMarshaller._
  
  // The JAXBContext is thread-safe
  private[this] val context: JAXBContext = {
    val classes: Set[Class[_]] = ClassUtil.findAnnotatedClasses(packageName, classOf[XmlRootElement])
    try {
      JAXBContext.newInstance(classes.toArray: _*)
    } catch {
      case ex: Exception => 
        logger.error(s"Caught exception trying to create JAXBContext for Jaxb2Marshaller($packageName) with classes: ${classes.map{_.getName}.mkString(", ")}", ex)
        throw ex
    }
  }
  
  // Marshaller is not thread-safe
  private[this] val marshaller: ThreadLocal[Marshaller] = new ThreadLocal[Marshaller] {
    override protected def initialValue: Marshaller = {
      val m: Marshaller = context.createMarshaller()
      m.setProperty(Marshaller.JAXB_FRAGMENT, fragment)
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format)
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
      
      try {
        // This probably won't work on non stock JAXB implementations
        m.setProperty("com.sun.xml.internal.bind.indentString", indent)
      } catch {
        case _: javax.xml.bind.PropertyException => // ignore
      }
      
      m
    }
  }
  
  // Unmarshaller is not thread-safe
  private[this] val unmarshaller: ThreadLocal[Unmarshaller] = new ThreadLocal[Unmarshaller] {
    override protected def initialValue: Unmarshaller = context.createUnmarshaller()
  }
  
  def toXML(obj: T): String = {
    val writer: StringWriter = new StringWriter()
    Resource.using(outputFactory.createXMLStreamWriter(writer)) { writeXML(obj, _) }
    writer.toString()
  }
  
  def toXML(obj: T, comments: XMLCommentProvider): String = {
    val writer: StringWriter = new StringWriter()
    Resource.using(outputFactory.createXMLStreamWriter(writer)) { writer: XMLStreamWriter =>
      val wrapped: CommentingXMLStreamWriter = new CommentingXMLStreamWriter(new IndentingXMLStreamWriter(writer), comments)
      writeXML(obj, wrapped)
    }
    writer.toString()
  }
  
  def writeXML(obj: T, os: OutputStream): Unit = writeXML(obj, os, "UTF-8")
  def writeXML(obj: T, os: OutputStream, encoding: String): Unit = Resource.using(outputFactory.createXMLStreamWriter(os, encoding)){ writeXML(obj, _) }
  
  def writeXML(obj: T, w: Writer): Unit = Resource.using(outputFactory.createXMLStreamWriter(w)){ writeXML(obj, _) }
  
  def writeXML(obj: T, writer: XMLStreamWriter): Unit = {
    val wrapped: XMLStreamWriter = if (format) IndentingXMLStreamWriter(writer, indent = indent) else writer
    marshaller.get().marshal(obj, wrapped)
  }
  
  def writeXML(obj: T, writer: IndentingXMLStreamWriter): Unit = marshaller.get().marshal(obj, writer)
  def writeXML(obj: T, writer: CommentingXMLStreamWriter): Unit = marshaller.get().marshal(obj, writer)

  def fromXML(xml: String): T = Resource.using(inputFactory.createXMLStreamReader(new StringReader(xml))){ readXML }

  def readXML(is: InputStream): T = Resource.using(inputFactory.createXMLStreamReader(is)){ readXML }
  def readXML(is: InputStream, encoding: String): T = Resource.using(inputFactory.createXMLStreamReader(is, encoding)){ readXML }
  
  def readXML(reader: Reader): T = Resource.using(inputFactory.createXMLStreamReader(reader)){ readXML }
  
  def readXML(reader: XMLStreamReader): T = unmarshaller.get().unmarshal(reader).asInstanceOf[T]
}
