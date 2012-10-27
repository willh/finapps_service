package Datastorage

import AppConfig.PayloadConfig
import com.mongodb.casbah.Imports._
import scala.Some

object PayloadStorage {
  def get(token: String): String = {
    val mongoColl = getCollection

    //val query = MongoDBObject("token" -> token)
    // return latest payload until twilio token-corrupting issue sorted
    val sortQuery = MongoDBObject("_id" -> -1)

    val results = mongoColl.find().sort(sortQuery).map {
      dbObj =>
        dbObj.getAsOrElse[String]("data", "NO_DATA")
    }
    val result = results.take(1)
    result.toList.head
  }

  def store(token: String, payload: String) {
    val mongoColl = getCollection

    val newData = MongoDBObject(
      "token" -> token,
      "data" -> payload
    )
    mongoColl.insert(newData, WriteConcern.Safe)
  }

  private def getCollection = {
    val url = PayloadConfig.getMongoURL
    url match {
      case None => {
        val conn = MongoConnection()
        conn("finapps")("payload")
      }
      case _ => {
        val conn = connectViaMongoUri.get
        conn("app8754822")("payload")
      }
    }
  }

  // sourced from https://github.com/typesafehub/webwords/
  private def connectViaMongoUri = {
    val uri: MongoURIParts = {
      val parsed = parseMongoURI(PayloadConfig.getMongoURL.getOrElse("mongodb:///")).get
      // "/" for the URI path means no database was in the URI
      if (parsed.database == Some("/") || parsed.database == Some(""))
        parsed.copy(database = None)
      else
        parsed
    }

    val connection = Some(MongoConnection(uri.host, uri.port))
    val dbname = uri.database.getOrElse("app8754822")

    val database = connection map {
      c => c(dbname)
    }

    uri.user foreach {
      username =>
        database.get.authenticate(username, uri.password.orNull)
    }
    connection
  }


  // the MongoURI class in mongo-java-parser is broken and blows up
  // with port, username, and password involved, so we need this.
  private def parseMongoURI(s: String): Option[MongoURIParts] = {

    val mongoDefaults = URIParts(scheme = "mongodb", host = Some("localhost"), port = Some(27017),
      user = None, password = None, path = None)
    UriParsing.expandURI(s, mongoDefaults) flatMap {
      parts =>
        if (parts.scheme != "mongodb")
          None
        else
          Some(MongoURIParts(user = parts.user, password = parts.password,
            host = parts.host.get, port = parts.port.get,
            database = parts.path))
    }
  }

}
