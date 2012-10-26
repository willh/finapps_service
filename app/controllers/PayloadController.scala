package controllers

import play.api._
import libs.json.Json
import play.api.mvc._
import com.mongodb.casbah.Imports._
import AppConfig.PayloadConfig

object PayloadController extends Controller {

  def test = Action {
    Ok(System.getenv("MONGOHQ_URL"))
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
    val mongoConn = MongoConnection(PayloadConfig.getMongoURL)
    val mongoColl = mongoConn("finapps")("payload")
    val query = MongoDBObject("token" -> token)

    mongoColl.findOne(query).map { dbObj =>
      dbObj.getAsOrElse[String]("data", "NO_DATA")
    }.getOrElse("NO_PAYLOAD")
  }

  private def store(token: String, payload: String) {
    val mongoConn = MongoConnection(PayloadConfig.getMongoURL)
    val mongoColl = mongoConn("finapps")("payload")

    val newData = MongoDBObject(
      "token"-> token,
      "data" -> payload
    )
    mongoColl += newData
  }

}
