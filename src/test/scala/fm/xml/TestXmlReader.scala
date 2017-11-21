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

import org.scalatest.{FunSuite, Matchers}

final class TestXmlReader extends FunSuite with Matchers {

  test("getRootElementName") {
    XmlReader.getRootElementName("") should equal (None)
    XmlReader.getRootElementName(null) should equal (None)
    XmlReader.getRootElementName(" ") should equal (None)
    XmlReader.getRootElementName("foo") should equal (None)
    XmlReader.getRootElementName("  <foo bar  ") should equal (None)

    XmlReader.getRootElementName("<foo>") should equal (Some("foo"))
    XmlReader.getRootElementName("<foo/>") should equal (Some("foo"))
    XmlReader.getRootElementName("<foo></bar>") should equal (Some("foo"))
    XmlReader.getRootElementName("<foo></bar>not valid xml<bar></foo>") should equal (Some("foo"))
    XmlReader.getRootElementName("<foo><bar>valid xml</bar></foo>") should equal (Some("foo"))
  }
  
}