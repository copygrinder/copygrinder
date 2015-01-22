/*
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
package org.copygrinder.pure.copybean.model

import scala.collection.immutable.ListMap

case class CopybeanFieldDef(
 id: String,
 displayName: String,
 `type`: FieldType.FieldType,
 attributes: Option[ListMap[String, Any]] = None,
 validators: Option[Seq[CopybeanFieldValidatorDef]] = None
 )

object FieldType extends Enumeration {

  type FieldType = Value

  val String, Integer, Reference, File = Value

}