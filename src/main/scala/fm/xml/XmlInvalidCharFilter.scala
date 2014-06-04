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

import com.sun.org.apache.xerces.internal.util.XMLChar
import fm.common.InvalidCharFilterReader
import java.io.Reader

final case class XmlInvalidCharFilter(r: Reader) extends InvalidCharFilterReader(r, true) {
  // TODO: Just import the source java file for com.sun.org.apache.xerces.internal.util.XMLChar
  //       so we aren't depending on internal sun classes.
  def isValidChar(ch: Char): Boolean = XMLChar.isValid(ch)
}