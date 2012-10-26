package controllers

import Datastorage.{UriParsing, URIParts, MongoURIParts}
import play.api._
import libs.json.Json
import play.api.mvc._
import com.mongodb.casbah.Imports._
import AppConfig.PayloadConfig
import com.mongodb.casbah.MongoURI

object PayloadController extends Controller {

  def test = Action {
    Ok("Hello services")
  }

  def storePayload = Action {
    implicit request =>
      request.body.asJson.map {
        request =>
          (request \ "payload").asOpt[Map[String, String]].map {
            payload =>

              val data = payload.get("data").getOrElse("NO_DATA")
              val token = payload.get("token").getOrElse("NO_TOKEN")

              store(token, data)
              Ok("Saved " + data)
          }.getOrElse {
            BadRequest("Expecting json body object called 'payload'")
          }
      }.getOrElse {
        BadRequest("Expecting json body")
      }
  }

  def getPayload(token: Option[String]) = Action {
    token.map {
      idToken =>
        val responseMap = Map("data" -> get(idToken))
        Ok(Json.toJson(responseMap)).as("application/json")
    }.getOrElse {
      BadRequest("Expecting token parameter")
    }

  }

  private def get(token: String): String = {
    val mongoColl = getCollection

    val query = MongoDBObject("token" -> token)

    mongoColl.findOne(query).map {
      dbObj =>
        dbObj.getAsOrElse[String]("data", "NO_DATA")
    }.getOrElse("NO_PAYLOAD")
  }

  private def store(token: String, payload: String) {
    val mongoColl = getCollection

    val newData = MongoDBObject(
      "token" -> token,
      "data" -> payload
    )
    mongoColl += newData
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



