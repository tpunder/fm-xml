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

import com.ctc.wstx.stax.WstxInputFactory
import fm.common.Implicits._
import fm.common.{Logging, Resource}
import fm.lazyseq.ResourceLazySeq
import java.io.{InputStream, Reader, StringReader}
import javax.xml.bind.{JAXBContext, Unmarshaller}
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import org.codehaus.stax2.XMLStreamReader2
import org.codehaus.stax2.util.StreamReader2Delegate
import scala.reflect.{ClassTag, classTag}
import RichXMLStreamReader2.toRichXMLStreamReader2
import scala.util.Try

object XmlReader {

  private val inputFactory: WstxInputFactory = new WstxInputFactory()
  inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
  inputFactory.configureForSpeed()

  import Resource._

  def withXMLStreamReader2[T](s: String)(f: XMLStreamReader2 => T): T = {
    Resource.using(inputFactory.createXMLStreamReader(new StringReader(s)).asInstanceOf[XMLStreamReader2])(f)
  }

  def withXMLStreamReader2[T](is: InputStream)(f: XMLStreamReader2 => T): T = {
    Resource.using(inputFactory.createXMLStreamReader(is).asInstanceOf[XMLStreamReader2])(f)
  }

  /**
   * Attempt to get the root element name from the passed in XML.
   *
   * Note: This is really more of a "getFirstElementName" since it will just return the first element it finds
   *       even if if it not a valid XML document.  e.g. "<foo></bar>" will return "foo"
   *
   * @param xml The XML
   * @return The root element name or None if there is none
   */
  def getRootElementName(xml: String): Option[String] = {
    if (xml.isNullOrBlank) return None

    withXMLStreamReader2(xml){ reader: XMLStreamReader2 =>
      Try {
        reader.seekToRootElement()
        reader.getLocalName
      }.toOption
    }
  }
    
  /**
   * Overrides the defaultNamespaceURI on elements/attributes.  This is used for the PIES feeds when the default namespace is not set
   * in the feed but JAXB expects the default namespace to be "http://www.aftermarket.org"
   */
  private class DefaultNamespaceStreamReaderDelegate(self: XMLStreamReader2, defaultNamespaceURI: String) extends StreamReader2Delegate(self) {
    Predef.require(defaultNamespaceURI.isNotNullOrBlank, "Expected defaultNamespace to be not blank")
    
    override def getNamespaceURI(): String = {
      val ns: String = super.getNamespaceURI()
      if (null == ns) defaultNamespaceURI else ns
    }
    
  }
  
  /**
   * Similar to DefaultNamespaceStreamReaderDelegate but just completely overrides the namespace.  Used when people set the namespace 
   * to something like "www.aftermarket.org" when it should be "http://www.aftermarket.org"
   */
  private class OverrideDefaultNamespaceStreamReaderDelegate(self: XMLStreamReader2, overrideDefaultNamespaceURI: String) extends StreamReader2Delegate(self) {
    Predef.require(overrideDefaultNamespaceURI.isNotNullOrBlank, "Expected overrideDefaultNamespaceURI to be not blank")
    
    override def getNamespaceURI(): String = {
      // If the prefix is blank (which I thinks means we are using the default namespace) use the override
      if (getPrefix().isNullOrBlank) overrideDefaultNamespaceURI else super.getNamespaceURI()
    }
  }
}

/**
 * defaultNamespaceURI - If no namespace is set, use this as a default
 * overrideDefaultNamespaceURI - Override the namespace and just use this instead
 */
class XmlReader[T: ClassTag](rootName: String, itemPath: String, defaultNamespaceURI: String, overrideDefaultNamespaceURI: String, protected val resource: Resource[Reader]) extends ResourceLazySeq[T, Reader] with Logging {
  
  if (defaultNamespaceURI.isNotNullOrBlank) require(overrideDefaultNamespaceURI.isNullOrBlank, "Can't set both defaultNamespaceURI and overrideDefaultNamespaceURI")
  if (overrideDefaultNamespaceURI.isNotNullOrBlank) require(defaultNamespaceURI.isNullOrBlank, "Can't set both defaultNamespaceURI and overrideDefaultNamespaceURI")
  
  private[this] val itemClass: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]
  private[this] val jaxbContext: JAXBContext = JAXBContext.newInstance(itemClass)
  
  // The XPath-like path to the element we are interested in
  // part => Array("part"), items/part => Array("items","part")
  private[this] val path: Array[String] = itemPath.split('/')
  
  // The name of the element we care about (last part of the path)
  // items/part => part
  private[this] val itemName: String = path.last
  
  private[this] val targetDepth: Int = path.length - 1
  
  private def wrapXMLStreamReader2(r: XMLStreamReader2): XMLStreamReader2 = {
    if (overrideDefaultNamespaceURI.isNotNullOrBlank) new XmlReader.OverrideDefaultNamespaceStreamReaderDelegate(r, overrideDefaultNamespaceURI)
    else if (defaultNamespaceURI.isNotNullOrBlank) new XmlReader.DefaultNamespaceStreamReaderDelegate(r, defaultNamespaceURI)
    else r
  }
  
  protected def foreachWithResource[U](f: T => U, reader: Reader): Unit = {
    val unmarshaller: Unmarshaller = jaxbContext.createUnmarshaller()
    val inputFactory = new WstxInputFactory()
    inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
    inputFactory.configureForSpeed()
    
    import Resource.toCloseable
    
    Resource.using(wrapXMLStreamReader2(inputFactory.createXMLStreamReader(XmlInvalidCharFilter(reader)).asInstanceOf[XMLStreamReader2])) { xmlStreamReader: XMLStreamReader2 =>
      // The current depth we are in the tree 
      // (which should correspond with the itemPath index we are currently interested in)
      var currentDepth: Int = 0

      // Move to the ROOT element (skipping stuff like DTDs) and check it's name and then move to the next parsing event
      xmlStreamReader.seekFirstEventPastRootElement(rootName)
      
      def seekToNextElement(): Unit = currentDepth = seekToNextElementImpl(xmlStreamReader, currentDepth)
     
      // Move to the next relevant tag
      seekToNextElement()
      
      while (xmlStreamReader.isStartElement && currentDepth >= 0) {
        xmlStreamReader.require(START_ELEMENT, null, itemName)
        val item = unmarshaller.unmarshal(xmlStreamReader, itemClass).getValue
        f(item)
        seekToNextElement()
      }
      
      consumeRestOfStream(xmlStreamReader)
    }
  }
  
  /**
   * Seeks to the next item element we are looking for
   */
  protected def seekToNextElementImpl(xmlStreamReader: XMLStreamReader2, _currentDepth: Int): Int = {
    var currentDepth: Int = _currentDepth
    var done: Boolean = false

    while (!done) {
      if (xmlStreamReader.isStartElement) {
        // We found the parent or child element we are looking for
        if(xmlStreamReader.getLocalName() == path(currentDepth)) {
          
          if (currentDepth == targetDepth) {
            // This is the child element we are looking for
            done = true
          } else {
            // This is a parent element of whatever child we are looking for, go down into it's children
            currentDepth += 1
          }
          
        } else {
          // This isn't an element we care about, skip it (including anything under it)
          xmlStreamReader.skipElement()
        }        
      } else if (xmlStreamReader.isEndElement) {
        currentDepth -= 1
        
        // If the currentDepth goes negative then we should have reached the end of the XML document
        if (currentDepth < 0) done = true
      }
      
      // Advance to the next event
      if (xmlStreamReader.hasNext() && !done) xmlStreamReader.next() else done = true
    }
    
    currentDepth
  }
  
  protected def consumeRestOfStream(xmlStreamReader: XMLStreamReader2): Unit = {
    var done: Boolean = false
    
    while (!done && xmlStreamReader.hasNext()) {
      try {
        xmlStreamReader.next()
        if(xmlStreamReader.isStartElement()) logger.warn("Unexpected start element: "+xmlStreamReader.getLocalName())
        if(xmlStreamReader.isEndElement()) logger.warn("Unexpected end element: "+xmlStreamReader.getLocalName())
      } catch {
        case ex: Exception => 
          logger.error("Caught Exception reader XML after we've already seen the closing tag: "+ex.getMessage)
          done = true
      }
    }
  }
}

