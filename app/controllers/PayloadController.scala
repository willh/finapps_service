package controllers

import play.api._
import libs.json.Json
import play.api.mvc._
import com.mongodb.casbah.Imports._
import AppConfig.PayloadConfig
import java.net.URI
import com.mongodb.casbah.MongoURI

object PayloadController extends Controller {

  def test = Action {
    Ok("Hello services")
  }

  def storePayload = Action {
    implicit request =>
      request.body.asJson.map { request =>
        (request \ "payload").asOpt[Map[String, String]].map { payload =>

          val data = payload.get("data").getOrElse("NO_DATA")
          val token = payload.get("token").getOrElse("NO_TOKEN")

          store(token, data)
          Ok("Saved "+ data)
        }.getOrElse {
          BadRequest("Expecting json body object called 'payload'")
        }
      }.getOrElse {
        BadRequest("Expecting json body")
      }
  }

  def getPayload(token: Option[String]) = Action {
    token.map { idToken =>
      val responseMap = Map("data" -> get(idToken))
      Ok(Json.toJson(responseMap)).as("application/json")
    }.getOrElse {
      BadRequest("Expecting token parameter")
    }

  }

  private def get(token: String): String = {
    val mongoColl = getCollection

    val query = MongoDBObject("token" -> token)

    mongoColl.findOne(query).map { dbObj =>
      dbObj.getAsOrElse[String]("data", "NO_DATA")
    }.getOrElse("NO_PAYLOAD")
  }

  private def store(token: String, payload: String) {
    val mongoColl = getCollection

    val newData = MongoDBObject(
      "token"-> token,
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
        val mongoUri = MongoURI(url.get)
        val conn = MongoConnection(mongoUri)
        val db = conn.getDB("app8754822")
        db.authenticate(mongoUri.username, new String(mongoUri.password))
        conn("app8754822")("payload")
      }
    }
  }

}
