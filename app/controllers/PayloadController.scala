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
              Ok("Saved payload").withHeaders(
                "Access-Control-Allow-Origin" -> "*",
                "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
                "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With, Accept",
                // cache access control response for one day
                "Access-Control-Max-Age" -> (60 * 60 * 24).toString
              )
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
        val responseMap = Map("data" -> Json.parse(PayloadStorage.get(idToken)))
        Ok(Json.toJson(responseMap)).as("application/json").withHeaders(
          "Access-Control-Allow-Origin" -> "*",
          "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
          "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With, Accept",
          // cache access control response for one day
          "Access-Control-Max-Age" -> (60 * 60 * 24).toString
        )
    }.getOrElse {
      BadRequest("Expecting token parameter")
    }

  }

}



