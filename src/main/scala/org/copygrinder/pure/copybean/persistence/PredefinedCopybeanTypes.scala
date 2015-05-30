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
package org.copygrinder.pure.copybean.persistence

import org.copygrinder.pure.copybean.model._

import scala.collection.immutable.ListMap
import scala.language.implicitConversions

class PredefinedCopybeanTypes {

  implicit def value2option[T](v: T): Option[T] = Option(v)

  lazy val predefinedTypes = List(typeType, copygrinderAdminType, classBackedValidatorType, fileMetadataType)
   .map(beanType => beanType.id -> beanType).toMap

  lazy val typeType: CopybeanTypeImpl = new CopybeanTypeImpl(
    enforcedTypeIds = Set(),
    id = "type",
    types = Set(),
    content = ListMap(
      "typeId" -> "type",
      "displayName" -> "Copybean Type",
      "instanceNameFormat" -> "$displayName$",
      "fields" -> Seq(
        ListMap("id" -> "typeId", "type" -> "String", "displayName" -> "Type ID", "validators" -> Seq(
          ListMap("type" -> "required")
        )),
        ListMap("id" -> "cardinality", "type" -> "String", "displayName" -> "Cardinality", "validators" -> Seq(
          ListMap("type" -> "required")
        )),
        ListMap("id" -> "displayName", "type" -> "String", "displayName" -> "Display Name"),
        ListMap("id" -> "instanceNameFormat", "type" -> "String", "displayName" -> "Instance Name Format"),
        ListMap("id" -> "fields", "type" -> "List", "displayName" -> "Fields", "attributes" -> ListMap(
          "listType" -> "Map")
        )
      )
    )
  )

  val copygrinderAdminType = CopybeanType(
    "copygrinderAdminMetatype",
    displayName = "Copygrinder Admin Metabean",
    fields = Seq(
      CopybeanFieldDef.cast("siloName", FieldType.String, "Silo Name", validators = Seq(
        new CopybeanFieldValidatorDef("required")
      ))
    ),
    cardinality = Cardinality.One
  )


  val classBackedValidatorType = CopybeanType(
    "classBackedFieldValidator",
    displayName = "Class Backed Field Validator Type",
    instanceNameFormat = "$displayName$",
    fields = Seq(
      CopybeanFieldDef.cast("class", FieldType.String, "Class", validators = Seq(
        new CopybeanFieldValidatorDef("required")
      ))
    ),
    cardinality = Cardinality.One
  )

  val fileMetadataType = CopybeanType(
    "fileMetadata",
    displayName = "File Metadata Type",
    instanceNameFormat = "$displayName$",
    fields = Seq(
      CopybeanFieldDef.cast("filename", FieldType.String, "File Name",
        validators = Seq(
          new CopybeanFieldValidatorDef("required")
        )),
      CopybeanFieldDef.cast("hash", FieldType.String, "Hash", validators = Seq(
        new CopybeanFieldValidatorDef("required")
      )),
      CopybeanFieldDef.cast("sizeInBytes", FieldType.Long, "Size in Bytes", validators = Seq(
        new CopybeanFieldValidatorDef("required")
      )),
      CopybeanFieldDef.cast("contentType", FieldType.String, "Content Type",
        validators = Seq(
          new CopybeanFieldValidatorDef("required")
        )
      )
    ),
    cardinality = Cardinality.One
  )

}