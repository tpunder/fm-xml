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

import java.io.{Reader, StringReader}
import org.scalatest.{FunSuite, Matchers}

class TestXmlInvalidCharFilter extends FunSuite with Matchers {
  def check(readFrom: String, readInto: String = null, offset: Int = 0, len: Int = -1): String = {
    val reader: Reader = XmlInvalidCharFilter(new StringReader(readFrom))
    val charactersToRead = if(len < 0) readFrom.size else len
    val buf: Array[Char] = if (null == readInto) new Array[Char](readFrom.length+offset) else readInto.toArray
    val read: Int = reader.read(buf, offset, charactersToRead)
    // If readInto was null then just trim the resulting buffer to whatever we read
    val res: Array[Char] = if (null == readInto) buf.slice(offset, read) else buf
    new String(res)
  }

  test("XMLInvalidCharFilterReader") {
    check("abc\u0000") should equal ("abc")  // Trailing Invalid
    check("\u0000abc") should equal("abc")   // Prefix Invalid
    check("ab\u0000c") should equal("abc")   // Middle Invalid
    check("ab\u0000\u0000c") should equal("abc") // Together Invalid
    check("a\u0000b\u0000c") should equal("abc") // Separate Invalid
    
    // Test Offset Reading
    check("foo", "ZZ...ZZ", offset = 2) should equal("ZZfooZZ")
    check("foo", "ZZ..ZZ", offset = 2, len = 2) should equal("ZZfoZZ")
    check("f\u0000o", "ZZ...", offset = 2, len = 3) should equal("ZZfo\u0000")
    check("f\u0000o", "ZZ...ZZ", offset = 2, len = 3) should equal("ZZfo\u0000ZZ")
    
    // Verify Lengths Limits are enforced
    check("a\u0000b\u0000c", len = 2) should equal("a")
    check("a\u0000b\u0000c", len = 3) should equal("ab")
    check("a\u0000b\u0000c", len = 4) should equal("ab")
  }
}