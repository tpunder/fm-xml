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
import fm.xml.RichXMLStreamReader2.toRichXMLStreamReader2
import java.io.{InputStream, Reader, StringReader}
import javax.xml.bind.{JAXBContext, Unmarshaller}
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import org.codehaus.stax2.XMLStreamReader2
import org.codehaus.stax2.util.StreamReader2Delegate
import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}
import scala.util.Try

object XmlReader {
  // Backward source-compatible support for specifying a simple itemPath/element type
  def apply[T: ClassTag](rootName: String, itemPath: String, defaultNamespaceURI: String, overrideDefaultNamespaceURI: String, resource: Resource[Reader]): XmlReader[T] = {
    val path: XmlReaderPath[T, T] = XmlReaderPath[T, T](itemPath, identity(_))

    new XmlReader[T](
      rootName,
      defaultNamespaceURI,
      overrideDefaultNamespaceURI,
      resource,
      IndexedSeq(path)
    )
  }

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


object XmlReaderPath {
  // Apply helper for reading a non-mapped value
  def apply[XmlValue: ClassTag](itemPath: String): XmlReaderPath[XmlValue, XmlValue] = XmlReaderPath[XmlValue, XmlValue](itemPath, identity(_))
}

/**
  *
  * @param itemPath The XPath-like path to the element we are interested in.  (e.g. "part", "items/part", etc)
  * @param toMappedValue Map the XmlValue into another return type, this should match the return type in MultiXmlReader
  * @tparam XmlValue The unmarshalled element class type
  * @tparam MappedXmlValue A mapped XmlValue type
  */
final case class XmlReaderPath[
XmlValue: ClassTag,
MappedXmlValue,
](itemPath: String, toMappedValue: XmlValue => MappedXmlValue) extends Logging {
  // Make this public, so we can use it for the XmlWriter JAXBContext
  val itemClass: Class[XmlValue] = classTag[XmlValue].runtimeClass.asInstanceOf[Class[XmlValue]]
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

  // TODO: Cleanup the matches/pathMatches
  // Check to see if the target depth and path match
  private[xml] def elementMatches(xmlStreamReader: XMLStreamReader2, currentDepth: Int): Boolean = {
    currentDepth == targetDepth && xmlStreamReader.getLocalName === itemName
  }

  private[xml] def pathMatches(xmlStreamReader: XMLStreamReader2, currentPath: mutable.ArrayStack[String]): Boolean = {
    if (currentPath.length > targetDepth) return false
    if (currentPath.length === targetDepth && (xmlStreamReader.getLocalName !== itemName)) return false

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
      if (currentPath(currentPath.length - 1 - depth) !== path(depth)) return false

      depth += 1
    }

    true
  }
}


/**
  * defaultNamespaceURI - If no namespace is set, use this as a default
  * overrideDefaultNamespaceURI - Override the namespace and just use this instead
  */
class XmlReader[T](
  rootName: String,
  defaultNamespaceURI: String,
  overrideDefaultNamespaceURI: String,
  protected val resource: Resource[Reader],
  targets: IndexedSeq[XmlReaderPath[_, T]]
) extends ResourceLazySeq[T, Reader] with Logging {
  if (defaultNamespaceURI.isNotNullOrBlank) require(overrideDefaultNamespaceURI.isNullOrBlank, "Can't set both defaultNamespaceURI and overrideDefaultNamespaceURI")
  if (overrideDefaultNamespaceURI.isNotNullOrBlank) require(defaultNamespaceURI.isNullOrBlank, "Can't set both defaultNamespaceURI and overrideDefaultNamespaceURI")

  private def wrapXMLStreamReader2(r: XMLStreamReader2): XMLStreamReader2 = {
    if (overrideDefaultNamespaceURI.isNotNullOrBlank) new XmlReader.OverrideDefaultNamespaceStreamReaderDelegate(r, overrideDefaultNamespaceURI)
    else if (defaultNamespaceURI.isNotNullOrBlank) new XmlReader.DefaultNamespaceStreamReaderDelegate(r, defaultNamespaceURI)
    else r
  }

  protected def foreachWithResource[U](f: T => U, reader: Reader): Unit = {

    val inputFactory = new WstxInputFactory()
    inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
    inputFactory.configureForSpeed()

    import Resource.toCloseable

    Resource.using(wrapXMLStreamReader2(inputFactory.createXMLStreamReader(XmlInvalidCharFilter(reader)).asInstanceOf[XMLStreamReader2])) { xmlStreamReader: XMLStreamReader2 =>
      val currentPath: mutable.ArrayStack[String] = new mutable.ArrayStack[String]
      var done: Boolean = false

      // Move to the ROOT element (skipping stuff like DTDs) and check it's name and then move to the next parsing event
      xmlStreamReader.seekFirstEventPastRootElement(rootName)

      while (!done) {
        var doNext: Boolean = true

        if (xmlStreamReader.isStartElement) {
          // Do we have paths that match?
          val matchingPaths: IndexedSeq[XmlReaderPath[_, T]] = targets.filter { _.pathMatches(xmlStreamReader, currentPath) }

          // We found the parent or child element we are looking for
          if (matchingPaths.nonEmpty) {

            // Optimized to avoid a closure
            var i: Int = 0
            while (doNext && i < matchingPaths.length) {
              val candidatePath: XmlReaderPath[_, T] = matchingPaths(i)

              if (candidatePath.elementMatches(xmlStreamReader, currentPath.length)) {
                f(candidatePath.readValue(xmlStreamReader))

                doNext = false // Unmarshalling the element will consume this endElement too
              }
              i += 1
            }

            // Did not find a match, but this is a parent element of whatever child we are looking for, go down into it's children
            if (doNext) {
              currentPath.push(xmlStreamReader.getLocalName)
            }
          } else {
            // This isn't an element we care about, skip it (including anything under it)
            xmlStreamReader.skipElement()
          }
        } else if (xmlStreamReader.isEndElement) {
          if (currentPath.nonEmpty) currentPath.pop()
          else done = true // If the currentPath goes negative then we should have reached the end of the XML document
        }

        if (doNext) {
          if (xmlStreamReader.hasNext() && !done) xmlStreamReader.next() else done = true
        }
      }

      consumeRestOfStream(xmlStreamReader)
    }
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

