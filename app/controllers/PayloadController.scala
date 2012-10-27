package controllers

import Datastorage.PayloadStorage
import play.api._
import libs.json.{JsValue, Json}
import play.api.mvc._
import play.Logger

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

  def getPayload = Action {
    implicit request => {

      request.body.asFormUrlEncoded.map {
        body =>
          val token = body.get("token").get.head
          val payload = PayloadStorage.get(token)
          val responseMap = payload match {
            case "NO_PAYLOAD" => Map("data" -> Json.parse("{\"error\": \"NO_TOKEN_MATCH\"}"))
            case _ => Map("data" -> Json.parse(payload))
          }
          Ok(Json.toJson(responseMap)).as("application/json").withHeaders(
            "Access-Control-Allow-Origin" -> "*",
            "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
            "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With, Accept, Origin",
            "Access-Control-Max-Age" -> (60 * 60 * 24).toString
          )
      }.getOrElse {
        BadRequest("Bad format of request, could not find request parameters").withHeaders(
          "Access-Control-Allow-Origin" -> "*",
          "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
          "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With, Accept, Origin",
          "Access-Control-Max-Age" -> (60 * 60 * 24).toString
        )
      }

    }
  }

}



