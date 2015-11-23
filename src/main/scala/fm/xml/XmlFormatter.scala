/*
 * Copyright 2015 Frugal Mechanic (http://frugalmechanic.com)
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

import fm.common.Resource
import fm.common.Resource.toCloseable
import java.io.{Reader, StringReader, StringWriter, Writer}
import javax.xml.stream.{XMLStreamReader, XMLStreamWriter}
import javax.xml.transform.{Transformer, TransformerFactory}
import javax.xml.transform.stax.{StAXResult, StAXSource}
import com.ctc.wstx.stax.{WstxInputFactory, WstxOutputFactory}

object XmlFormatter {
  private val wstxInputFactory: WstxInputFactory = new WstxInputFactory()
  wstxInputFactory.configureForSpeed()
  
  private val wstxOutputFactory: WstxOutputFactory = new WstxOutputFactory()
  wstxOutputFactory.configureForSpeed()
  
  private val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
  
  private def makeXMLStreamReader(in: Reader): XMLStreamReader = {
    val reader: XMLStreamReader = wstxInputFactory.createXMLStreamReader(in)
    reader
  }
  
  private def makeXMLStreamWriter(out: Writer): XMLStreamWriter = {
    val writer: XMLStreamWriter = IndentingXMLStreamWriter(wstxOutputFactory.createXMLStreamWriter(out, "UTF-8"))
    writer
  }
  
  def format(in: String): String = {
    format(new StringReader(in))
  }
  
  def format(in: Reader): String = {
    val out: StringWriter = new StringWriter()
    format(in, out)
    out.toString()
  }
  
  def format(in: Reader, out: Writer): Unit = {
    Resource.using(makeXMLStreamReader(in)) { sr: XMLStreamReader =>
      Resource.using(makeXMLStreamWriter(out)) { sw: XMLStreamWriter =>
        format(sr, sw)
      }
    }
  }
  
  def format(in: XMLStreamReader, out: XMLStreamWriter): Unit = {
    val t: Transformer = transformerFactory.newTransformer()
    val source: StAXSource = new StAXSource(in)
    val result: StAXResult = new StAXResult(out)
    t.transform(source, result)
  }  
}