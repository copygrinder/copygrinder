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
package org.copygrinder.impure.copybean.persistence.backend.impl

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import org.apache.commons.io.FileUtils
import org.copygrinder.pure.collections.ImmutableLinkedHashMap
import org.copygrinder.pure.copybean.exception._
import org.mapdb.{DBMaker, TxMaker}

import scala.collection.JavaConversions._
import scala.concurrent._

class MapDbDao(silo: String, storageDir: File) {

  protected val txMaker = new AtomicReference[Option[TxMaker]](None)

  protected def getTxMaker(allowNew: Boolean = false) = {
    if (txMaker.get().isEmpty) {

      if (!allowNew && (!storageDir.exists() || storageDir.list().isEmpty)) {
        throw new SiloNotInitialized(silo)
      }

      FileUtils.forceMkdir(storageDir)
      val newDb = DBMaker
       .newFileDB(new File(storageDir, "datastore.mapdb"))
       .closeOnJvmShutdown()
       .cacheLRUEnable()
       .makeTxMaker()

      val success = txMaker.compareAndSet(None, Some(newDb))
      if (success) {
        newDb
      } else {
        newDb.close()
        txMaker.get().get
      }
    } else {
      txMaker.get().get
    }
  }

  def initSilo()(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      blocking {
        if (storageDir.exists() && storageDir.list().nonEmpty) {
          throw new SiloAlreadyInitialized(silo)
        }
        getTxMaker(allowNew = true)
      }
    }
  }

  def get[R](tableId: String, key: String)(implicit ec: ExecutionContext): Future[R] = {
    getOpt(tableId, key).map(_.getOrElse(throw new DaoReadException(tableId, key)))
  }

  def getOpt[R](tableId: String, key: String)(implicit ec: ExecutionContext): Future[Option[R]] = {
    Future {
      blocking {
        val tx = getTxMaker().makeTx()
        val table = tx.createHashMap(tableId).makeOrGet[String, R]
        val result = Option(table.get(key))
        tx.close()
        result
      }
    }
  }

  def getCompositeOpt[R](tableId: String, key: (String, String))(implicit ec: ExecutionContext): Future[Option[R]] = {
    getOpt[Map[String, R]](tableId, key._1).map { nestedTableOpt =>
      nestedTableOpt.flatMap { nestedTable =>
        nestedTable.get(key._2)
      }
    }
  }

  def getCompositeKeySet(tableId: String)(implicit ec: ExecutionContext): Future[Set[(String, String)]] = {
    Future {
      blocking {
        val tx = getTxMaker().makeTx()
        val table = tx.createHashMap(tableId).makeOrGet[String, Map[String, Any]]
        val result = table.flatMap { case (outerKey, entrySet) =>
          entrySet.keySet.map(innerKey => (outerKey, innerKey))
        }.toSet
        tx.close()
        result
      }
    }
  }


  def getSeqOpt[R](tableId: String, keys: Seq[String])
   (implicit ec: ExecutionContext): Future[ImmutableLinkedHashMap[String, Option[R]]] = {
    Future {
      blocking {
        val tx = getTxMaker().makeTx()
        val table = tx.createHashMap(tableId).makeOrGet[String, R]
        val entries = keys.map { key =>
          key -> Option(table.get(key))
        }
        tx.close()
        new ImmutableLinkedHashMap ++ entries
      }
    }
  }

  def getSeq[R](tableId: String, keys: Seq[String])
   (implicit ec: ExecutionContext): Future[ImmutableLinkedHashMap[String, R]] = {
    getSeqOpt[R](tableId, keys).map { resultMap =>
      resultMap.map { case (key, nodeOpt) =>
        if (nodeOpt.isDefined) {
          key -> nodeOpt.get
        } else {
          throw new DaoReadException(tableId, key)
        }
      }
    }
  }

  def setComposite[T](tableId: String, key: (String, String), value: T)
   (implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      blocking {
        val tx = getTxMaker().makeTx()
        val table = tx.createHashMap(tableId).makeOrGet[String, Map[String, T]]
        val mapOpt = Option(table.get(key._1))
        val map = if (mapOpt.isDefined) {
          mapOpt.get
        } else {
          Map[String, T]()
        }
        val newMap = map.updated(key._2, value)
        table.put(key._1, newMap)
        tx.commit()
        tx.close()
      }
    }
  }

  def set[T](tableId: String, key: String, value: T)(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      blocking {
        val tx = getTxMaker().makeTx()
        val table = tx.createHashMap(tableId).makeOrGet[String, T]
        table.put(key, value)
        tx.commit()
        tx.close()
      }
    }
  }

}