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
import org.codehaus.stax2.XMLStreamReader2
import org.scalatest.{FunSuite, Matchers}
import java.io.StringReader
import javax.xml.stream.{XMLInputFactory, XMLStreamException}
import javax.xml.stream.XMLStreamConstants.START_ELEMENT

import RichXMLStreamReader2.toRichXMLStreamReader2

final class TestRichXMLStreamReader2 extends FunSuite with Matchers {
  
  test("seekToRootElement()") {
    val sr: XMLStreamReader2 = createSR()
    sr.seekToRootElement("root")
    sr.getDepth should equal (1)
    sr.getEventType should equal (START_ELEMENT)
    sr.getLocalName() should equal ("root")
  }
  
  test("seekToSiblingElement - Exception") {
    val sr: XMLStreamReader2 = createSR()
    sr.seekToRootElement()
    intercept[XMLStreamException] { sr.seekToSiblingElement("header") }
  }
  
  test("seekToChildElement()") {
    val sr: XMLStreamReader2 = createSR()
    sr.seekToRootElement()
    sr.seekToChildElement("items")
  }
  
  test("seekToChildElement() - Exception") {
    val sr: XMLStreamReader2 = createSR()
    sr.seekToRootElement()
    intercept[XMLStreamException] { sr.seekToChildElement("items_foo") }
  }
  
  test("seekToNextSiblingElement()") {
    val sr: XMLStreamReader2 = createSR()
    sr.seekToRootElement()
    sr.seekToChildElement()
    sr.getLocalName() should equal ("header")
    sr.seekToSiblingElement()
    sr.getLocalName() should equal ("items")
    sr.seekToSiblingElement()
    sr.getLocalName() should equal ("trailer")
    intercept[XMLStreamException] { sr.seekToSiblingElement() }
  }
  
  test("Simple Document Traversing") {
    val sr: XMLStreamReader2 = createSR()
    sr.seekToRootElement()
    sr.seekToChildElement("items")
    sr.seekToChildElement("item")
    sr.readChildElementText("name") should equal ("Item 1 Name")
    sr.seekToSiblingElement("item")
    sr.seekToEndOfParentElement()
    sr.seekToSiblingElement("trailer")
    sr.seekToChildElement("name")
    sr.readElementText() should equal ("Trailer Name")
  }
  
  test("foreach - root/items/item") {
    val sr: XMLStreamReader2 = createSR()
    val builder = Vector.newBuilder[String]
    
    sr.foreach("root/items/item") {
      builder += sr.readChildElementText("name")
    }
    
    builder.result should equal (Vector("Item 1 Name", "Item 2 Name"))
  }
  
  test("foreach - root/items/item/name") {
    val sr: XMLStreamReader2 = createSR()
    val builder = Vector.newBuilder[String]
    
    sr.foreach("root/items/item/name") {
      builder += sr.readElementText()
    }
    
    builder.result should equal (Vector("Item 1 Name", "Item 2 Name"))
  }

  test("readElementAsXMLString - 1") {
    val sr: XMLStreamReader2 = createSR()

    sr.seekToRootElement()
    sr.seekToChildElement("items")
    sr.readElementAsXMLString should equal (
      """<items>
        |    <item idx="1">
        |      <name>Item 1 Name</name>
        |    </item>
        |    <item idx="2">
        |      <name>Item 2 Name</name>
        |    </item>
        |    <items foo="bar">
        |      <item idx="1">
        |        <name>Sub Item 1 name</name>
        |      </item>
        |    </items>
        |  </items>""".stripMargin)

    sr.seekToSiblingElement()
    sr.getLocalName() should equal ("trailer")
  }

  test("readElementAsXMLString - 2") {
    val sr: XMLStreamReader2 = createSR()

    sr.seekToRootElement()
    sr.seekToChildElement("items") // The outer <items> element
    sr.seekToChildElement("items") // The inner nested <items> element
    sr.readElementAsXMLString should equal (
      """<items foo="bar">
        |      <item idx="1">
        |        <name>Sub Item 1 name</name>
        |      </item>
        |    </items>""".stripMargin)

    sr.seekToEndOfParentElement() // Gets us back to the outer <items> element
    sr.seekToSiblingElement() // Should advance us to the <trailer> element
    sr.getLocalName() should equal ("trailer")
  }
  
  private def createSR(): XMLStreamReader2 = {
    val inputFactory: WstxInputFactory = new WstxInputFactory()
    inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
    inputFactory.configureForSpeed()
    inputFactory.createXMLStreamReader(new StringReader(xml)).asInstanceOf[XMLStreamReader2]
  }
  
val xml = """
<?xml version='1.0' encoding='UTF-8'?>
<root>
  <header>
    <name>Header Name</name>
  </header>
  <items>
    <item idx="1">
      <name>Item 1 Name</name>
    </item>
    <item idx="2">
      <name>Item 2 Name</name>
    </item>
    <items foo="bar">
      <item idx="1">
        <name>Sub Item 1 name</name>
      </item>
    </items>
  </items>
  <trailer>
    <name>Trailer Name</name>
  </trailer>
</root>
""".trim
}