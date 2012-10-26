package Datastorage

import java.net.URI
import java.net.URISyntaxException

object UriParsing {

  def expandURI (s: String, defaults: URIParts): Option[URIParts] = {
    try {
      val uri = new URI (s)

      val host = Option (uri.getHost) orElse (defaults.host)
      val port = (if (uri.getPort == - 1) None else Some (uri.getPort) ) orElse (defaults.port)

      // URI has the "/" in front of the path but URIParts strips it off.
      val path = Option (stripSlash (uri.getPath) ) orElse (defaults.path)
      val userInfo = Option (uri.getUserInfo)
      val (user, password) = userInfo map {
        ui =>
          if (ui.contains (":") ) {
            val a = ui.split (":", 2)
            (Some (a (0) ) -> Some (a (1) ) )
          } else {
            (Some (ui) -> defaults.password)
          }
      } getOrElse (defaults.user -> defaults.password)

      Some (URIParts (scheme = uri.getScheme, user = user, password = password,
        host = host, port = port, path = path) )
    } catch {
      case e: URISyntaxException =>
        None
    }
  }

  private def stripSlash(s: String) =
    if (s == "")
      null
    else if (s.startsWith("/"))
      s.substring(1)
    else
      s
}

case class MongoURIParts(user: Option[String], password: Option[String],
                         host: String, port: Int, database: Option[String])

case class URIParts(scheme: String, user: Option[String], password: Option[String],
                    host: Option[String], port: Option[Int], path: Option[String])
