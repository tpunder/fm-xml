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

import java.io.StringReader
import org.scalatest.{FunSuite, Matchers}
import scala.beans.BeanProperty

final class SimpleFeedPart {

  @BeanProperty var uniqueId: String = _
  @BeanProperty var source: String = _
  @BeanProperty var name: String = _
}

final class TestXmlReaderWriter extends FunSuite with Matchers {
  
  private def verifySimple(xml: String) {
    val reader = makeReader(xml)
    reader.hasNext should equal(true)
    checkOne(reader.next)
    reader.hasNext should equal(true)
    checkTwo(reader.next)
    reader.hasNext should equal(false)
  }
  
  private val simplePartXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed>
  <!-- Comments Should be Ignored -->
  <part>
    <uniqueId>ONE</uniqueId>
    <source>foo</source>
    <name>Simple Part One</name>
  </part>
  <foo><part><uniqueId>SKIPME</uniqueId></part></foo>
  <part>
    <uniqueId>TWO</uniqueId>
    <source>foo</source>
    <name>Simple Part Two</name>
  </part>
  <!-- Comments Should be Ignored -->
</feed>
"""

  test("Simple Part - Reading") {
    verifySimple(simplePartXml)
  }
  
    private val simplePartOneLineXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part></feed>
"""
  
  test("Simple Part One Line - Reading") {
    verifySimple(simplePartOneLineXml)
  }

    private val simplePartOneLineExtraClosingTagXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part></feed></feed>
"""
  
  test("Simple Part One Line with Extra Closing Tag - Reading") {
    verifySimple(simplePartOneLineExtraClosingTagXml)
  }

  private val simplePartOneLineXmlWithoutXmlDecl = """
<feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part></feed>
""".trim
  
  test("Simple Part One Line Without XML Declaration - Reading") {
    verifySimple(simplePartOneLineXmlWithoutXmlDecl)
  }

  private val simplePartOneLineXmlWithDTD = """<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE ACES SYSTEM "ACES.dtd"><feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part></feed>
""".trim

  test("Simple Part One Line with DTD - Reading") {
    verifySimple(simplePartOneLineXmlWithDTD)
  }

    private val simplePartOneLineXmlWithDTDAndComments = """<?xml version='1.0' encoding='UTF-8'?><!-- foo --><!DOCTYPE ACES SYSTEM "ACES.dtd"><feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part></feed>
""".trim

  test("Simple Part One Line with DTD and Comments - Reading") {
    verifySimple(simplePartOneLineXmlWithDTDAndComments)
  }

  private val mixedPartXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed>
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
  <one>
    <uniqueId>ONE</uniqueId>
    <source>foo</source>
    <name>Simple Part One</name>
  </one>
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
  <part>
    <uniqueId>SKIPME</uniqueId>
  </part>
  <two>
    <uniqueId>TWO</uniqueId>
    <source>foo</source>
    <name>Simple Part Two</name>
  </two>
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
</feed>
"""
  
  test("Selective Element Reading") {
    // This should only read the <one> element
    val oneReader = makeReader(mixedPartXml, item="one")
    oneReader.hasNext should equal(true)
    checkOne(oneReader.next)
    oneReader.hasNext should equal(false)
    
    // This should only read the <two> element
    val twoReader = makeReader(mixedPartXml, item="two")
    twoReader.hasNext should equal(true)
    checkTwo(twoReader.next)
    twoReader.hasNext should equal(false)
  }
  
  private val nestedPartXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed>
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
  <items>
    <part>
      <uniqueId>ONE</uniqueId>
      <source>foo</source>
      <name>Simple Part One</name>
    </part>
    <foo><part><uniqueId>SKIPME</uniqueId></part></foo>
    <part>
      <uniqueId>TWO</uniqueId>
      <source>foo</source>
      <name>Simple Part Two</name>
    </part>
  </items>
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
</feed>
"""
  test("Nested Part - Reading") {
    val reader = makeReader(nestedPartXml, item="items/part")    
    reader.hasNext should equal(true)    
    checkOne(reader.next)
    reader.hasNext should equal(true)
    checkTwo(reader.next)
    reader.hasNext should equal(false)
  }
  
    private val deepNestedPartXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed>
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
  <items>
    <items2>
      <items3>
        <part>
          <uniqueId>ONE</uniqueId>
          <source>foo</source>
          <name>Simple Part One</name>
        </part>
        <foo><part><uniqueId>SKIPME</uniqueId></part></foo>
        <part>
          <uniqueId>TWO</uniqueId>
          <source>foo</source>
          <name>Simple Part Two</name>
        </part>
      </items3>
    </items2>
  </items>
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
</feed>
"""
  test("Deep Nested Part - Reading") {
    val reader = makeReader(deepNestedPartXml, item="items/items2/items3/part")    
    reader.hasNext should equal(true)    
    checkOne(reader.next)
    reader.hasNext should equal(true)
    checkTwo(reader.next)
    reader.hasNext should equal(false)
  }
    
    
  private val multiNestedPartXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed>
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
  <items>
    <part>
      <uniqueId>ONE</uniqueId>
      <source>foo</source>
      <name>Simple Part One</name>
    </part>
    <foo><part><uniqueId>SKIPME</uniqueId></part></foo>
    <part>
      <uniqueId>TWO</uniqueId>
      <source>foo</source>
      <name>Simple Part Two</name>
    </part>
  </items>
  <!-- hello -->
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
  <items>
    <part>
      <!-- hello -->
      <uniqueId>ONE</uniqueId>
      <source>foo</source>
      <name>Simple Part One</name>
    </part>
    <foo><part><uniqueId>SKIPME</uniqueId></part></foo>
    <part>
      <uniqueId>TWO</uniqueId>
      <source>foo</source>
      <name>Simple Part Two</name>
      <!-- hello -->
    </part>
  </items>
  <!-- hello -->
</feed>
"""
  test("Multi-Nested Part - Reading") {
    val reader = makeReader(multiNestedPartXml, item="items/part")    
    reader.hasNext should equal(true)    
    checkOne(reader.next)
    reader.hasNext should equal(true)
    checkTwo(reader.next)
    reader.hasNext should equal(true)    
    checkOne(reader.next)
    reader.hasNext should equal(true)
    checkTwo(reader.next)
    reader.hasNext should equal(false)
  }
  
  private val emptyFeedXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed>
</feed>
"""
    
  test("Empty Feed") {
    val reader = makeReader(emptyFeedXml, item="items/part")
    reader.hasNext should equal(false)
  }
    
  test("Feed that has elements but not what we are looking for") {
    val reader = makeReader(nestedPartXml, item="items/element_that_does_not_exist")
    reader.hasNext should equal(false)
  }
    
  private val checkOne = checkPart(_: SimpleFeedPart, "ONE", "foo", "Simple Part One")
  private val checkTwo = checkPart(_: SimpleFeedPart, "TWO", "foo", "Simple Part Two")
    
  private def checkPart(part: SimpleFeedPart, uniqueId: String, source: String, name: String) {
    part.uniqueId should equal(uniqueId)
    part.source should equal(source)
    part.name should equal(name)
  }
  
  private def makeReader(xml: String, root: String = "feed", item: String = "part"): Iterator[SimpleFeedPart] = XmlReaderWriter[SimpleFeedPart](root, item).reader(new StringReader(xml)).toIndexedSeq.iterator
}