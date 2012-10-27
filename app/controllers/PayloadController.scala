package controllers

import Datastorage.PayloadStorage
import play.api._
import libs.json.{JsValue, Json}
import play.api.mvc._

object PayloadController extends Controller {

  def test = Action {
    Ok("Hello services")
  }

  def storePayload = Action {
    implicit request =>
      request.body.asJson.map {
        request => {
          // parse out the token and data pieces separately;
          // we want token as string but json sub-object of data as a string
          val token = (request \ "payload" \ "token").asOpt[String].getOrElse("NO_TOKEN")
          (request \ "payload" \ "data").asOpt[JsValue].map {
            data => {
              PayloadStorage.store(token, Json.stringify(data))
              Ok("Saved payload")
            }
          }.getOrElse {
            BadRequest("Expecting json body object 'payload' with 'data' property")
          }
        }
      }.getOrElse {
        BadRequest("Expecting json body")
      }
  }

  def getPayload(token: Option[String]) = Action {
    token.map {
      idToken =>
        val responseMap = Map("data" -> PayloadStorage.get(idToken))
        Ok(Json.toJson(responseMap)).as("application/json")
    }.getOrElse {
      BadRequest("Expecting token parameter")
    }

  }

}



