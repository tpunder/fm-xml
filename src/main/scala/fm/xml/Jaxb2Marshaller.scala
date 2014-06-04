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

import fm.common.{ClassUtil, Logging}
import java.io.{StringReader, StringWriter}
import javax.xml.bind._
import javax.xml.bind.annotation._
import javax.xml.transform.stream.{StreamResult,StreamSource}

object Jaxb2Marshaller {
  def apply(packageName: String) = new Jaxb2Marshaller(packageName)
}

sealed trait MarshallerOption
case object XmlPretty extends MarshallerOption
case object XmlFragment extends MarshallerOption

class Jaxb2Marshaller(val packageName: String) extends Logging {
  def this(packageName: String, options: MarshallerOption*) {
    this(packageName)
    options.foreach{ o =>
      o match {
        case XmlPretty => doFormatted = true
        case XmlFragment => doFragment = true
      }
    }
  }

  private val context: JAXBContext = {
    val classes = ClassUtil.findAnnotatedClasses(packageName, classOf[XmlRootElement])
    try {
      JAXBContext.newInstance(classes.toArray: _*)
    } catch {
      case ex: Exception => 
        logger.error(s"Caught exception trying to create JAXBContext for Jaxb2Marshaller($packageName) with classes: ${classes.map{_.getName}.mkString(", ")}", ex)
        throw ex
    }
  }
  
  private val marshaller: Marshaller = context.createMarshaller()
  private val unmarshaller: Unmarshaller = context.createUnmarshaller()
  
  setMarshallerProperties()

  private var doFragment = false
  private var doFormatted = false

  private def setMarshallerProperties() {
    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, doFragment)
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, doFormatted)
    marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
  }

  def fragment: Jaxb2Marshaller = fragment(true)
  def fragment(b:Boolean): Jaxb2Marshaller = {
    doFragment = b
    setMarshallerProperties()
    this
  }

  def pretty: Jaxb2Marshaller = pretty(true)
  def pretty(b:Boolean): Jaxb2Marshaller = {
    doFormatted = b
    setMarshallerProperties()
    this
  }

  def fromXml[T](xml:String): T = {
    unmarshaller.unmarshal(new StreamSource(new StringReader(xml))).asInstanceOf[T]
  }

  def toXml(o:Any): String = {
    val sw = new StringWriter
    marshaller.marshal(o, new StreamResult(sw))
    sw.toString
  }

  def toXml(o:Any, os:java.io.OutputStream) {
    marshaller.marshal(o, new StreamResult(os))
  }
}

/*
class Jaxb2Marshaller(val packageName:String) {
  def this(packageName:String, options:MarshallerOption*) {
    this(packageName)
    options.foreach{ o =>
      o match {
        case XmlPretty => pretty
        case XmlFragment => fragment
      }
    }
  }

  lazy val context: JAXBContext = JAXBContext.newInstance(packageName)
  lazy val marshaller: Marshaller = context.createMarshaller
  lazy val unmarshaller: Unmarshaller = context.createUnmarshaller

  def fragment: Jaxb2Marshaller = fragment(true)
  def fragment(b:Boolean): Jaxb2Marshaller = {
    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, b)
    this
  }

  def pretty: Jaxb2Marshaller = pretty(true)
  def pretty(b:Boolean): Jaxb2Marshaller = {
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, b)
    this
  }

  def fromXml[T](xml:String): T = {
    unmarshaller.unmarshal(new StringReader(xml)).asInstanceOf[T]
  }

  def toXml(o:Any): String = {
    val sw = new StringWriter
    marshaller.marshal(o, sw)
    sw.toString
  }
}
*/
