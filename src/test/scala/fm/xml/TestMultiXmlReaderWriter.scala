/*
 * Copyright 2018 Frugal Mechanic (http://frugalmechanic.com)
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

import fm.common.Logging
import java.io.StringReader
import org.scalatest.{FunSuite, Matchers}
import scala.beans.BeanProperty


final class SimpleFeedPrice {
  @BeanProperty var uniqueId: String = _
  @BeanProperty var price: Double = _
}

// This is mostly a clone of `TestXmlReaderWriter`, except reading multiple different elements
final class TestMultiXmlReaderWriter extends FunSuite with Matchers with Logging {
  private def verifySimple(xml: String) {

    val reader = makeReader(xml)
    reader.hasNext should equal(true)

    val one = reader.next
    checkOnePart(one)
    reader.hasNext should equal(true)

    val onePrice = reader.next
    checkOnePrice(onePrice)
    reader.hasNext should equal(true)

    val two = reader.next
    checkTwoPart(two)
    reader.hasNext should equal(true)

    val twoPrice = reader.next
    checkTwoPrice(twoPrice)

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
  <price>
    <uniqueId>ONE</uniqueId>
    <price>9.99</price>
  </price>
  <foo><part><uniqueId>SKIPME</uniqueId></part></foo>
  <part>
    <uniqueId>TWO</uniqueId>
    <source>foo</source>
    <name>Simple Part Two</name>
  </part>
  <price>
    <uniqueId>TWO</uniqueId>
    <price>19.99</price>
  </price>
  <!-- Comments Should be Ignored -->
</feed>
"""

  test("Simple Part - Reading") {
    verifySimple(simplePartXml)
  }

  private val simplePartOneLineXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><price><uniqueId>ONE</uniqueId><price>9.99</price></price><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part><price><uniqueId>TWO</uniqueId><price>19.99</price></price></feed>
"""

  test("Simple Part One Line - Reading") {
    verifySimple(simplePartOneLineXml)
  }

  private val simplePartOneLineExtraClosingTagXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><price><uniqueId>ONE</uniqueId><price>9.99</price></price><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part><price><uniqueId>TWO</uniqueId><price>19.99</price></price></feed></feed>
"""

  test("Simple Part One Line with Extra Closing Tag - Reading") {
    verifySimple(simplePartOneLineExtraClosingTagXml)
  }

  private val simplePartOneLineXmlWithoutXmlDecl = """
<feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><price><uniqueId>ONE</uniqueId><price>9.99</price></price><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part><price><uniqueId>TWO</uniqueId><price>19.99</price></price></feed>
""".trim

  test("Simple Part One Line Without XML Declaration - Reading") {
    verifySimple(simplePartOneLineXmlWithoutXmlDecl)
  }

  private val simplePartOneLineXmlWithDTD = """<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE ACES SYSTEM "ACES.dtd"><feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><price><uniqueId>ONE</uniqueId><price>9.99</price></price><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part><price><uniqueId>TWO</uniqueId><price>19.99</price></price></feed>""".trim

  test("Simple Part One Line with DTD - Reading") {
    verifySimple(simplePartOneLineXmlWithDTD)
  }

  private val simplePartOneLineXmlWithDTDAndComments = """<?xml version='1.0' encoding='UTF-8'?><!-- foo --><!DOCTYPE ACES SYSTEM "ACES.dtd"><feed><part><uniqueId>ONE</uniqueId><source>foo</source><name>Simple Part One</name></part><price><uniqueId>ONE</uniqueId><price>9.99</price></price><part><uniqueId>TWO</uniqueId><source>foo</source><name>Simple Part Two</name></part><price><uniqueId>TWO</uniqueId><price>19.99</price></price></feed>""".trim

  test("Simple Part One Line with DTD and Comments - Reading") {
    verifySimple(simplePartOneLineXmlWithDTDAndComments)
  }

  private val mixedPartXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed>
  <foo>
    <bar>asdasd</bar>
    <part>
      <uniqueId>SKIPME</uniqueId>
    </part>
    <price>
      <uniqueId>SKIPME</uniqueId>
    </price>
  </foo>
  <one>
    <uniqueId>ONE</uniqueId>
    <source>foo</source>
    <name>Simple Part One</name>
  </one>
  <onePrice>
    <uniqueId>ONE</uniqueId>
    <price>9.99</price>
  </onePrice>
  <foo>
    <bar>asdasd</bar>
    <part>
      <uniqueId>SKIPME</uniqueId>
    </part>
  </foo>
  <foo>
    <bar>asdasd</bar>
    <price>
      <uniqueId>SKIPME</uniqueId>
    </price>
  </foo>
  <two>
    <uniqueId>TWO</uniqueId>
    <source>foo</source>
    <name>Simple Part Two</name>
  </two>
  <twoPrice>
    <uniqueId>TWO</uniqueId>
    <price>19.99</price>
  </twoPrice>
  <foo>
    <bar>asdasd</bar>
    <part>
      <uniqueId>SKIPME</uniqueId>
    </part>
  </foo>
</feed>
"""

  test("Selective Element Reading") {
    // This should only read the <one> element
    val oneReader = makeReader(mixedPartXml, paths=Seq(OnePartPath))
    oneReader.hasNext should equal(true)
    checkOnePart(oneReader.next)
    oneReader.hasNext should equal(false)

    // This should only read the <two> element
    val twoReader = makeReader(mixedPartXml, paths=Seq(TwoPartPath))
    twoReader.hasNext should equal(true)
    checkTwoPart(twoReader.next)
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
    val reader = makeReader(nestedPartXml, paths=Seq(NestedItemsPartPath, NestedItemsPricePath))
    reader.hasNext should equal(true)
    checkOnePart(reader.next)
    reader.hasNext should equal(true)
    checkTwoPart(reader.next)
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
        <price>
          <uniqueId>ONE</uniqueId>
          <price>9.99</price>
        </price>
        <foo><part><uniqueId>SKIPME</uniqueId></part></foo>
        <part>
          <uniqueId>TWO</uniqueId>
          <source>foo</source>
          <name>Simple Part Two</name>
        </part>
        <price>
          <uniqueId>TWO</uniqueId>
          <price>19.99</price>
        </price>
      </items3>
    </items2>
  </items>
  <foo><bar>asdasd</bar><part><uniqueId>SKIPME</uniqueId></part></foo>
</feed>
"""
  test("Deep Nested Part - Reading") {
    val reader = makeReader(deepNestedPartXml, paths=Seq(DeepNestedPartPath, DeepNestedPricePath))
    reader.hasNext should equal(true)
    checkOnePart(reader.next)

    reader.hasNext should equal(true)
    checkOnePrice(reader.next)

    reader.hasNext should equal(true)
    checkTwoPart(reader.next)

    reader.hasNext should equal(true)
    checkTwoPrice(reader.next)

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
    <price>
      <uniqueId>ONE</uniqueId>
      <price>9.99</price>
    </price>
    <foo><part><uniqueId>SKIPME</uniqueId></part></foo>
    <part>
      <uniqueId>TWO</uniqueId>
      <source>foo</source>
      <name>Simple Part Two</name>
    </part>
    <price>
      <uniqueId>TWO</uniqueId>
      <price>19.99</price>
    </price>
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
    <price>
      <!-- hello -->
      <uniqueId>ONE</uniqueId>
      <price>9.99</price>
    </price>
    <foo><part><uniqueId>SKIPME</uniqueId></part></foo>
    <part>
      <uniqueId>TWO</uniqueId>
      <source>foo</source>
      <name>Simple Part Two</name>
      <!-- hello -->
    </part>
    <price>
      <uniqueId>TWO</uniqueId>
      <price>19.99</price>
      <!-- hello -->
    </price>
  </items>
  <!-- hello -->
</feed>
"""
  test("Multi-Nested Part - Reading") {
    val reader = makeReader(multiNestedPartXml, paths=Seq(NestedItemsPartPath, NestedItemsPricePath))
    reader.hasNext should equal(true)
    checkOnePart(reader.next)

    reader.hasNext should equal(true)
    checkOnePrice(reader.next)

    reader.hasNext should equal(true)
    checkTwoPart(reader.next)

    reader.hasNext should equal(true)
    checkTwoPrice(reader.next)

    reader.hasNext should equal(true)
    checkOnePart(reader.next)

    reader.hasNext should equal(true)
    checkOnePrice(reader.next)

    reader.hasNext should equal(true)
    checkTwoPart(reader.next)

    reader.hasNext should equal(true)
    checkTwoPrice(reader.next)

    reader.hasNext should equal(false)
  }

  private val emptyFeedXml = """<?xml version='1.0' encoding='UTF-8'?>
<feed>
</feed>
"""

  test("Empty Feed") {
    val reader = makeReader(emptyFeedXml, paths=Seq(NestedItemsPartPath, NestedItemsPricePath))
    reader.hasNext should equal(false)
  }

  test("Feed that has elements but not what we are looking for") {
    val reader = makeReader(nestedPartXml, paths=Seq(NestedItemsPartElementNotExistPath, NestedItemsPriceElementNotExistPath))
    reader.hasNext should equal(false)
  }

  trait MyPathValue extends AnyRef

  case class PartPathValue(value: SimpleFeedPart) extends MyPathValue
  case class PricePathValue(value: SimpleFeedPrice) extends MyPathValue

  case class NestedItemsPartPathValue(value: SimpleFeedPart) extends MyPathValue
  case class NestedItemsPricePathValue(value: SimpleFeedPrice) extends MyPathValue

  case class NestedItemsPartElementNotExistPathValue(value: SimpleFeedPart) extends MyPathValue
  case class NestedItemsPriceElementNotExistPathValue(value: SimpleFeedPrice) extends MyPathValue

  case class DeepNestedPartPathValue(value: SimpleFeedPart) extends MyPathValue
  case class DeepNestedPricePathValue(value: SimpleFeedPrice) extends MyPathValue

  case class OnePartPathValue(value: SimpleFeedPart) extends MyPathValue
  case class OnePricePathValue(value: SimpleFeedPrice) extends MyPathValue

  case class TwoPartPathValue(value: SimpleFeedPart) extends MyPathValue
  case class TwoPricePathValue(value: SimpleFeedPrice) extends MyPathValue

  val PartPath = XmlReaderPath[SimpleFeedPart, MyPathValue]("part", PartPathValue(_))
  val PricePath = XmlReaderPath[SimpleFeedPrice, MyPathValue]("price", PricePathValue(_))

  val NestedItemsPartPath = XmlReaderPath[SimpleFeedPart, MyPathValue]("items/part", NestedItemsPartPathValue(_))
  val NestedItemsPricePath = XmlReaderPath[SimpleFeedPrice, MyPathValue]("items/price", NestedItemsPricePathValue(_))

  val NestedItemsPartElementNotExistPath = XmlReaderPath[SimpleFeedPart, MyPathValue]("items/element_that_does_not_exist", NestedItemsPartElementNotExistPathValue(_))
  val NestedItemsPriceElementNotExistPath = XmlReaderPath[SimpleFeedPrice, MyPathValue]("prices/element_that_does_not_exist", NestedItemsPriceElementNotExistPathValue(_))

  val DeepNestedPartPath = XmlReaderPath[SimpleFeedPart, MyPathValue]("items/items2/items3/part", DeepNestedPartPathValue(_))
  val DeepNestedPricePath = XmlReaderPath[SimpleFeedPrice, MyPathValue]("items/items2/items3/price", DeepNestedPricePathValue(_))

  val OnePartPath = XmlReaderPath[SimpleFeedPart, MyPathValue]("one", OnePartPathValue(_))
  val OnePricePath = XmlReaderPath[SimpleFeedPrice, MyPathValue]("onePrice", OnePricePathValue(_))

  val TwoPartPath = XmlReaderPath[SimpleFeedPart, MyPathValue]("two", TwoPartPathValue(_))
  val TwoPricePath = XmlReaderPath[SimpleFeedPrice, MyPathValue]("twoPrice", TwoPricePathValue(_))


  private val checkOnePart = checkValue(_: MyPathValue, "ONE", "foo", "Simple Part One")
  private val checkTwoPart = checkValue(_: MyPathValue, "TWO", "foo", "Simple Part Two")

  private val checkOnePrice = checkValue(_: MyPathValue, "ONE", expectedPrice = 9.99)
  private val checkTwoPrice = checkValue(_: MyPathValue, "TWO", expectedPrice = 19.99)

  private def checkValue(value: MyPathValue, uniqueId: String, source: String = "", name: String = "", expectedPrice: Double = 0d): Unit = {
    value match {
      case PartPathValue(part: SimpleFeedPart)                           => checkPart(part, uniqueId, source, name)
      case NestedItemsPartPathValue(part: SimpleFeedPart)                => checkPart(part, uniqueId, source, name)
      case NestedItemsPartElementNotExistPathValue(part: SimpleFeedPart) => checkPart(part, uniqueId, source, name)
      case DeepNestedPartPathValue(part: SimpleFeedPart)                 => checkPart(part, uniqueId, source, name)
      case OnePartPathValue(part: SimpleFeedPart)                        => checkPart(part, uniqueId, source, name)
      case TwoPartPathValue(part: SimpleFeedPart)                        => checkPart(part, uniqueId, source, name)

      case PricePathValue(price: SimpleFeedPrice)                           => checkPrice(price, uniqueId, expectedPrice)
      case NestedItemsPricePathValue(price: SimpleFeedPrice)                => checkPrice(price, uniqueId, expectedPrice)
      case NestedItemsPriceElementNotExistPathValue(price: SimpleFeedPrice) => checkPrice(price, uniqueId, expectedPrice)
      case DeepNestedPricePathValue(price: SimpleFeedPrice)                 => checkPrice(price, uniqueId, expectedPrice)
      case OnePricePathValue(price: SimpleFeedPrice)                        => checkPrice(price, uniqueId, expectedPrice)
      case TwoPricePathValue(price: SimpleFeedPrice)                        => checkPrice(price, uniqueId, expectedPrice)
    }
  }

  private def checkPart(part: SimpleFeedPart, uniqueId: String, source: String, name: String) {
    part.uniqueId should equal(uniqueId)
    part.source should equal(source)
    part.name should equal(name)
  }

  private def checkPrice(part: SimpleFeedPrice, uniqueId: String, price: Double) {
    part.uniqueId should equal(uniqueId)
    part.price should equal(price)
  }

  private def makeReader(xml: String, root: String = "feed", paths: Seq[XmlReaderPath[_, MyPathValue]] = Seq(PartPath, PricePath)): Iterator[MyPathValue] = {
    XmlReader[MyPathValue]("feed", new StringReader(xml), paths.head, paths.tail:_*).toIndexedSeq.iterator
  }
}