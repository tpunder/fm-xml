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

import fm.common.Implicits._

//object XMLCommentProvider {
//  def apply(leading: Map[String,String] = Map.empty, trailing: Map[String,String] = Map.empty): XMLCommentProvider = new XMLCommentProvider {
//    def leadingComment(path: String, attributes: Map[String,String], value: Option[String]): Option[String] = leading.get(path).flatMap{ _.toBlankOption }
//    def trailingComment(path: String, attributes: Map[String,String], value: Option[String]): Option[String] = trailing.get(path).flatMap{ _.toBlankOption }
//  }
//}

trait XMLCommentProvider {
  def leadingComment(path: String, attributes: Map[String,String], value: Option[String]): Option[String]
  def trailingComment(path: String, attributes: Map[String,String], value: Option[String]): Option[String]
}